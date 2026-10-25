package com.rutagps.libre

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors

/** Solo pruebas de GPS mediante API oficial de Android. Sin root, anuncios ni mapas offline. */
class MainActivity : Activity() {
    private val bg = Color.rgb(16, 25, 35)
    private val surface = Color.rgb(27, 40, 54)
    private val accent = Color.rgb(42, 190, 157)
    private val secondary = Color.rgb(150, 168, 184)
    private val points = mutableListOf<RoutePoint>()
    private val dwells = mutableListOf<Int>() // Segundos por punto. Solo se aplican a los intermedios.
    private var geometry: List<RoutePoint>? = null
    private var prepared = false
    private var fixed = true
    private var speed = 35
    private var running = false
    private var paused = false
    private var pendingStart = false
    private var calculating = false
    private var lastRouting = 0L
    private var lastSpeedCommand = 0L
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var mapView: MapView
    private var map: MapLibreMap? = null
    private var currentPin: Marker? = null
    private lateinit var count: TextView
    private lateinit var status: TextView
    private lateinit var hint: TextView
    private lateinit var speedText: TextView
    private lateinit var speedSeek: SeekBar
    private lateinit var fixedButton: Button
    private lateinit var routeButton: Button
    private lateinit var pauseButton: Button
    private lateinit var routeControls: LinearLayout

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != MockLocationService.ACTION_STATUS) return
            running = intent.getBooleanExtra("running", false)
            paused = intent.getBooleanExtra("paused", false)
            status.text = intent.getStringExtra("message") ?: "Sin información"
            pauseButton.text = if (paused) "Reanudar" else "Pausar"
            if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
                showPosition(RoutePoint(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0)))
            }
            if (!running) clearPosition()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadDraft()
        // Identifica la app ante el proveedor de tiles y habilita caché HTTP normal.
        // La caché evita repetir descargas visibles; NO hay funciones de mapas offline.
        val tilesClient = OkHttpClient.Builder()
            .cache(Cache(File(cacheDir, "map_http"), 64L * 1024L * 1024L))
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "RutaGPSLibre/0.2.2 (Android; com.rutagps.libre)")
                    .build()
                chain.proceed(request)
            }.build()
        MapLibre.getInstance(this)
        HttpRequestUtil.setOkHttpClient(tilesClient)
        buildUi(savedInstanceState)
    }

    private fun buildUi(state: Bundle?) {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        setContentView(root)
        root.addView(text("RUTA GPS LIBRE", 21f, Color.WHITE, true).apply {
            setPadding(dp(16), dp(12), dp(16), dp(2))
        })
        hint = text("Toca el mapa para seleccionar un punto", 12f, secondary).apply {
            setPadding(dp(16), dp(2), dp(16), dp(8))
        }
        root.addView(hint)

        val holder = FrameLayout(this)
        root.addView(holder, LinearLayout.LayoutParams(-1, 0, 1f))
        mapView = MapView(this)
        holder.addView(mapView, FrameLayout.LayoutParams(-1, -1))
        holder.addView(text("© OpenStreetMap contributors", 11f, Color.WHITE).apply {
            setPadding(dp(7), dp(3), dp(7), dp(3))
            setBackgroundColor(Color.argb(180, 10, 18, 28))
        }, FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM or Gravity.END).apply {
            setMargins(0, 0, dp(7), dp(8))
        })
        mapView.onCreate(state)
        mapView.getMapAsync { m ->
            map = m
            m.uiSettings.isCompassEnabled = true
            m.uiSettings.isAttributionEnabled = true
            m.setPrefetchesTiles(false)
            m.moveCamera(CameraUpdateFactory.newLatLngZoom(
                if (points.isEmpty()) LatLng(-12.0464, -77.0428)
                else LatLng(points.last().lat, points.last().lon), 12.5
            ))
            m.addOnMapClickListener(MapLibreMap.OnMapClickListener { at ->
                onMapTap(RoutePoint(at.latitude, at.longitude))
                true
            })
            m.setStyle(Style.Builder().fromUri("asset://map_style.json")) {
                refreshMap()
            }
        }

        val scroll = ScrollView(this)
        root.addView(scroll, LinearLayout.LayoutParams(-1, dp(285)))
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(9), dp(12), dp(10))
            setBackgroundColor(surface)
        }
        scroll.addView(panel)
        val modes = row()
        fixedButton = button("📍 Fija") { selectMode(true) }
        routeButton = button("🛣 Ruta") { selectMode(false) }
        modes.addView(fixedButton, weight())
        modes.addView(routeButton, weight())
        panel.addView(modes)

        count = text("", 13f, Color.WHITE).apply { setPadding(dp(4), dp(8), 0, dp(4)) }
        panel.addView(count)
        val edit = row()
        edit.addView(button("Deshacer") {
            if (running) { show("Detén la simulación antes de editar"); return@button }
            if (points.isNotEmpty()) { points.removeAt(points.lastIndex); dwells.removeAt(dwells.lastIndex); invalidateRoute() }
        }, weight())
        edit.addView(button("Borrar puntos") {
            if (running) { show("Detén la simulación antes de editar"); return@button }
            points.clear(); dwells.clear(); invalidateRoute()
        }, weight())
        panel.addView(edit)
        panel.addView(button("+ Añadir coordenadas manualmente") { coordinateDialog() })

        routeControls = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val actions = row()
        actions.addView(button("Calcular por calles") { calculateRoadRoute() }, weight())
        actions.addView(button("Línea recta") {
            if (running) { show("Detén la simulación antes de editar"); return@button }
            if (points.size < 2) show("Marca inicio y destino") else {
                geometry = points.toList(); prepared = true
                status.text = "Ruta en línea recta lista"
                refreshMap()
            }
        }, weight())
        routeControls.addView(actions)
        routeControls.addView(button("⏱ Configurar paradas intermedias") { stopsDialog() })
        panel.addView(routeControls)

        val speedRow = row()
        speedRow.addView(text("Velocidad (también durante el recorrido)", 13f, Color.WHITE), weight())
        speedText = text("$speed km/h", 14f, Color.WHITE, true)
        speedRow.addView(speedText)
        panel.addView(speedRow)
        speedSeek = SeekBar(this).apply {
            max = 119; progress = speed - 1
            progressTintList = ColorStateList.valueOf(accent)
            thumbTintList = ColorStateList.valueOf(accent)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    speed = progress + 1
                    speedText.text = "$speed km/h"
                    if (fromUser && running && SystemClock.elapsedRealtime() - lastSpeedCommand >= 220L) sendSpeed()
                    if (fromUser) saveDraft()
                }
                override fun onStartTrackingTouch(bar: SeekBar?) = Unit
                override fun onStopTrackingTouch(bar: SeekBar?) { if (running) sendSpeed() }
            })
        }
        panel.addView(speedSeek)

        val player = row()
        player.addView(button("▶ Iniciar", true) { startClicked() }, weight())
        pauseButton = button("Pausar") {
            if (!running) show("Primero inicia la simulación")
            else sendCommand(if (paused) MockLocationService.ACTION_RESUME else MockLocationService.ACTION_PAUSE)
        }
        player.addView(pauseButton, weight())
        player.addView(button("■ Detener") { sendCommand(MockLocationService.ACTION_STOP) }, weight())
        panel.addView(player)
        status = text("Listo", 13f, accent).apply { setPadding(dp(4), dp(8), dp(4), dp(5)) }
        panel.addView(status)
        panel.addView(button("⚙ Configurar ubicación simulada en Android") {
            try { startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) }
            catch (_: Exception) { show("Busca Opciones de desarrollador en Ajustes") }
        })
        root.addView(text("Creado por Alexander", 11f, secondary).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(7))
        })
        selectMode(fixed, save = false)
    }

    private fun selectMode(value: Boolean, save: Boolean = true) {
        if (running && save) { show("Detén la simulación para cambiar de modo"); return }
        fixed = value
        fixedButton.backgroundTintList = ColorStateList.valueOf(if (fixed) accent else bg)
        routeButton.backgroundTintList = ColorStateList.valueOf(if (!fixed) accent else bg)
        fixedButton.setTextColor(if (fixed) bg else Color.WHITE)
        routeButton.setTextColor(if (!fixed) bg else Color.WHITE)
        routeControls.visibility = if (fixed) View.GONE else View.VISIBLE
        hint.text = if (fixed) "Toca el mapa para fijar tu ubicación" else
            "Toca inicio, paradas y destino (hasta 15 puntos)"
        if (fixed && points.size > 1) {
            val last = points.last()
            points.clear(); points.add(last)
            dwells.clear(); dwells.add(0)
            geometry = null; prepared = false
        }
        if (save) saveDraft()
        refreshMap()
    }

    private fun onMapTap(point: RoutePoint) {
        if (running) { show("Detén la simulación para editar puntos"); return }
        if (fixed) { points.clear(); dwells.clear() }
        if (points.size >= 15) { show("Máximo 15 puntos por recorrido"); return }
        points.add(point)
        dwells.add(0)
        invalidateRoute()
    }

    private fun coordinateDialog() {
        if (running) { show("Detén la simulación antes de editar"); return }
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), 0, dp(20), 0) }
        val flags = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        val latitude = EditText(this).apply { hint = "Latitud: -12.0464"; inputType = flags }
        val longitude = EditText(this).apply { hint = "Longitud: -77.0428"; inputType = flags }
        layout.addView(latitude); layout.addView(longitude)
        AlertDialog.Builder(this).setTitle("Añadir coordenadas")
            .setView(layout).setNegativeButton("Cancelar", null)
            .setPositiveButton("Añadir") { _, _ ->
                try {
                    val p = RoutePoint(latitude.text.toString().replace(',', '.').toDouble(),
                        longitude.text.toString().replace(',', '.').toDouble())
                    onMapTap(p); center(p)
                } catch (_: Exception) { show("Coordenadas inválidas") }
            }.show()
    }

    private fun stopsDialog() {
        if (fixed) { show("Las paradas se usan en rutas"); return }
        if (running) { show("Detén la simulación para cambiar paradas"); return }
        if (points.size < 3) { show("Añade al menos una parada entre el inicio y el destino"); return }
        val labels = (1 until points.lastIndex).map { i ->
            "Parada $i · ${if (dwells[i] == 0) "sin espera" else "${dwells[i]} s"} · ${format(points[i])}"
        }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Tiempo en cada parada")
            .setItems(labels) { _, idx ->
                val pointIndex = idx + 1
                val input = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_NUMBER
                    setText(dwells[pointIndex].toString())
                    selectAll()
                }
                AlertDialog.Builder(this).setTitle("Parada $pointIndex: duración en segundos")
                    .setMessage("0 = sin espera. Máximo 3600 segundos (60 minutos).")
                    .setView(input).setNegativeButton("Cancelar", null)
                    .setPositiveButton("Guardar") { _, _ ->
                        val seconds = input.text.toString().toIntOrNull()
                        if (seconds == null || seconds !in 0..3600) show("Introduce de 0 a 3600 segundos")
                        else { dwells[pointIndex] = seconds; saveDraft(); refreshMap(); show("Parada configurada: $seconds s") }
                    }.show()
            }.setNegativeButton("Cerrar", null).show()
    }

    private fun invalidateRoute() {
        geometry = null; prepared = false
        saveDraft(); refreshMap()
    }

    private fun refreshMap() {
        val m = map ?: return
        val style = m.style ?: return
        m.clear()
        currentPin = null
        points.forEachIndexed { i, p ->
            m.addMarker(MarkerOptions().position(LatLng(p.lat, p.lon)).title(
                if (fixed) "Ubicación fija" else when (i) {
                    0 -> "Inicio"
                    points.lastIndex -> "Destino"
                    else -> "Parada $i${if (dwells.getOrElse(i){0} > 0) " · ${dwells[i]} s" else ""}"
                }
            ))
        }
        val path = geometry ?: points
        val existing = style.getSourceAs<GeoJsonSource>("route-geojson")
        if (path.size >= 2) {
            val feature = Feature.fromGeometry(LineString.fromLngLats(path.map { Point.fromLngLat(it.lon, it.lat) }))
            if (existing == null) {
                style.addSource(GeoJsonSource("route-geojson", feature))
                style.addLayer(LineLayer("route-line", "route-geojson").withProperties(
                    lineColor(if (prepared) accent else Color.rgb(236, 172, 70)), lineWidth(4f)))
            } else {
                existing.setGeoJson(feature)
                style.getLayer("route-line")?.setProperties(
                    lineColor(if (prepared) accent else Color.rgb(236, 172, 70)))
            }
        } else if (existing != null) {
            existing.setGeoJson("""{"type":"FeatureCollection","features":[]}""")
        }
        count.text = if (fixed) {
            if (points.isEmpty()) "Toca el mapa para elegir una ubicación" else "Punto: ${format(points[0])}"
        } else "${points.size} puntos · ${dwells.count { it > 0 }} esperas · " + when {
            prepared -> "ruta lista para iniciar"
            points.size < 2 -> "marca inicio y destino"
            else -> "calcula la ruta o elige línea recta"
        }
    }

    private fun center(p: RoutePoint) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p.lat, p.lon), 15.0))
    }

    private fun calculateRoadRoute() {
        if (calculating) { show("Ya se está calculando una ruta"); return }
        if (running) { show("Detén la simulación antes de modificar"); return }
        if (points.size < 2) { show("Añade mínimo dos puntos"); return }
        if (SystemClock.elapsedRealtime() - lastRouting < 1200L) { show("Espera un segundo"); return }
        calculating = true; lastRouting = SystemClock.elapsedRealtime()
        val requested = points.toList()
        status.text = "Calculando recorrido por calles..."
        executor.execute {
            try {
                val result = RoadRouting.calculate(requested)
                runOnUiThread {
                    calculating = false
                    if (isDestroyed || requested != points) return@runOnUiThread
                    geometry = result; prepared = true
                    status.text = "Recorrido listo (${result.size} posiciones)"
                    refreshMap()
                }
            } catch (ex: Exception) {
                runOnUiThread {
                    calculating = false
                    if (!isDestroyed) status.text = "No fue posible calcular: ${ex.message}"
                }
            }
        }
    }

    private fun startClicked() {
        if (running) { show("Ya hay una simulación activa"); return }
        if (points.isEmpty() || (!fixed && points.size < 2)) {
            show(if (fixed) "Selecciona un lugar" else "Marca inicio y destino"); return
        }
        if (!fixed && !prepared) { show("Primero calcula una ruta o selecciona línea recta"); return }
        if (!hasMockPermission()) { showMockInstructions(); return }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            pendingStart = true
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 90)
            return
        }
        startSimulation()
    }

    private fun hasMockPermission(): Boolean = (getSystemService(APP_OPS_SERVICE) as AppOpsManager)
        .checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED

    private fun showMockInstructions() {
        AlertDialog.Builder(this).setTitle("Autoriza la ubicación de prueba")
            .setMessage("Abre Opciones de desarrollador → Elegir aplicación de ubicación simulada → Ruta GPS Libre.\n\nLas aplicaciones pueden detectar que la ubicación es simulada.")
            .setNegativeButton("Cerrar", null)
            .setPositiveButton("Abrir ajustes") { _, _ ->
                try { startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) }
                catch (_: Exception) { show("Busca Opciones de desarrollador en Ajustes") }
            }.show()
    }

    private fun startSimulation() {
        val path = if (fixed) points.take(1) else geometry ?: return
        val stops = if (fixed) emptyList() else StopPlanner.fromWaypoints(path, points, dwells)
        val encodedStops = JSONArray().apply {
            stops.forEach { put(JSONArray().put(it.atMeters).put(it.seconds)) }
        }
        val intent = Intent(this, MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_START
            putExtra(MockLocationService.EXTRA_ROUTE, RouteCodec.encode(path))
            putExtra(MockLocationService.EXTRA_STOPS, encodedStops.toString())
            putExtra(MockLocationService.EXTRA_SPEED, speed.toDouble())
            putExtra(MockLocationService.EXTRA_FIXED, fixed)
        }
        try {
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
            status.text = "Iniciando simulación..."
        } catch (e: Exception) { status.text = "No se pudo iniciar: ${e.message}" }
    }

    private fun sendSpeed() {
        lastSpeedCommand = SystemClock.elapsedRealtime()
        try {
            startService(Intent(this, MockLocationService::class.java).apply {
                action = MockLocationService.ACTION_SET_SPEED
                putExtra(MockLocationService.EXTRA_SPEED, speed.toDouble())
            })
        } catch (e: Exception) { show("No se pudo modificar la velocidad") }
    }

    private fun sendCommand(actionName: String) {
        try { startService(Intent(this, MockLocationService::class.java).setAction(actionName)) }
        catch (e: Exception) { show("No se pudo ejecutar la orden: ${e.message}") }
    }

    private fun showPosition(p: RoutePoint) {
        val m = map ?: return
        if (currentPin == null) {
            currentPin = m.addMarker(MarkerOptions().position(LatLng(p.lat, p.lon)).title("Posición simulada"))
        } else {
            currentPin?.position = LatLng(p.lat, p.lon)
            currentPin?.let { m.updateMarker(it) }
        }
    }
    private fun clearPosition() {
        currentPin?.let { map?.removeMarker(it) }
        currentPin = null
    }

    private fun saveDraft() {
        val waits = JSONArray().apply { dwells.forEach { put(it) } }
        getSharedPreferences("borrador", MODE_PRIVATE).edit()
            .putBoolean("fixed", fixed).putInt("speed", speed)
            .putString("points", if (points.isEmpty()) "[]" else RouteCodec.encode(points))
            .putString("waits", waits.toString()).apply()
    }

    private fun loadDraft() {
        val pref = getSharedPreferences("borrador", MODE_PRIVATE)
        fixed = pref.getBoolean("fixed", true)
        speed = pref.getInt("speed", 35).coerceIn(1, 120)
        try {
            val raw = pref.getString("points", "[]") ?: "[]"
            if (raw != "[]") points.addAll(RouteCodec.decode(raw).take(15))
            val waits = JSONArray(pref.getString("waits", "[]"))
            points.indices.forEach { dwells.add(if (it < waits.length()) waits.optInt(it, 0).coerceIn(0, 3600) else 0) }
        } catch (_: Exception) { points.clear(); dwells.clear() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 90 && pendingStart) {
            pendingStart = false
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) startSimulation()
            else show("Concede la ubicación precisa para activar la prueba")
        }
    }

    override fun onStart() {
        super.onStart(); mapView.onStart()
        val filter = IntentFilter(MockLocationService.ACTION_STATUS)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        else { @Suppress("DEPRECATION") registerReceiver(receiver, filter) }
        val pref = getSharedPreferences("estado", MODE_PRIVATE)
        running = pref.getBoolean("running", false)
        paused = pref.getBoolean("paused", false)
        status.text = pref.getString("message", "Listo para configurar")
        pauseButton.text = if (paused) "Reanudar" else "Pausar"
    }
    override fun onResume() { super.onResume(); if (::mapView.isInitialized) mapView.onResume() }
    override fun onPause() { if (::mapView.isInitialized) mapView.onPause(); super.onPause() }
    override fun onStop() { unregisterReceiver(receiver); mapView.onStop(); super.onStop() }
    override fun onDestroy() { executor.shutdownNow(); if (::mapView.isInitialized) mapView.onDestroy(); super.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); if (::mapView.isInitialized) mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); if (::mapView.isInitialized) mapView.onSaveInstanceState(outState) }

    private fun row() = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
    private fun weight() = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
    private fun button(title: String, highlight: Boolean = false, onClick: () -> Unit): Button = Button(this).apply {
        text = title
        textSize = 12f
        isAllCaps = false
        setTextColor(if (highlight) bg else Color.WHITE)
        backgroundTintList = ColorStateList.valueOf(if (highlight) accent else Color.rgb(45, 62, 81))
        setOnClickListener { onClick() }
    }
    private fun text(label: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply {
        text = label; textSize = size; setTextColor(color)
        if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
    }
    private fun format(p: RoutePoint): String = String.format(Locale.US, "%.5f, %.5f", p.lat, p.lon)
    private fun dp(pixels: Int): Int = (pixels * resources.displayMetrics.density).toInt()
    private fun show(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
