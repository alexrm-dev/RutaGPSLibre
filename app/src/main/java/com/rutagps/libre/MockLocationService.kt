package com.rutagps.libre

import android.Manifest
import android.app.Notification
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import java.util.Locale

/** Publica ubicaciones MOCK con las API oficiales. Nunca intenta ocultar la marca isMock(). */
class MockLocationService : Service() {
    companion object {
        const val ACTION_START = "com.rutagps.libre.START"
        const val ACTION_PAUSE = "com.rutagps.libre.PAUSE"
        const val ACTION_RESUME = "com.rutagps.libre.RESUME"
        const val ACTION_STOP = "com.rutagps.libre.STOP"
        const val ACTION_SET_SPEED = "com.rutagps.libre.SET_SPEED"
        const val EXTRA_STOPS = "stops"
        const val ACTION_STATUS = "com.rutagps.libre.STATUS"
        const val EXTRA_ROUTE = "route"
        const val EXTRA_SPEED = "speed"
        const val EXTRA_FIXED = "fixed"
        private const val CHANNEL = "simulacion"
        private const val ID = 31
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var locationManager: LocationManager
    private val registeredProviders = mutableSetOf<String>()
    private var cursor: RouteCursor? = null
    private var playback: RoutePlayback? = null
    private var kmh = 25.0
    private var fixed = false
    private var paused = false
    private var lastUpdate = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val notifications = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notifications.createNotificationChannel(NotificationChannel(CHANNEL, "Simulación GPS", NotificationManager.IMPORTANCE_LOW))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> begin(intent)
            ACTION_PAUSE -> {
                if (cursor != null) {
                    paused = true
                    report("En pausa")
                    updateNotification("Simulación en pausa")
                } else stopSelf()
            }
            ACTION_RESUME -> {
                if (cursor != null) {
                    paused = false
                    lastUpdate = SystemClock.elapsedRealtime()
                    report("Simulando")
                    updateNotification("Simulando ubicación")
                } else stopSelf()
            }
            ACTION_SET_SPEED -> {
                if (cursor != null) {
                    kmh = intent.getDoubleExtra(EXTRA_SPEED, kmh).coerceIn(1.0, 120.0)
                    playback?.speedKmh = kmh
                    report("Velocidad actual: ${kmh.toInt()} km/h")
                }
            }
            ACTION_STOP -> {
                report("Simulación detenida", running = false)
                stopSelf()
            }
            else -> if (cursor == null) stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun begin(intent: Intent) {
        try {
            val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            if (appOps.checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, Process.myUid(), packageName)
                != AppOpsManager.MODE_ALLOWED) {
                error("Selecciona Ruta GPS Libre como app de ubicación simulada en Opciones de desarrollador")
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                error("Concede permiso de ubicación para activar la simulación")
            }
            val points = RouteCodec.decode(intent.getStringExtra(EXTRA_ROUTE) ?: "[]")
            kmh = intent.getDoubleExtra(EXTRA_SPEED, 25.0).coerceIn(1.0, 120.0)
            fixed = intent.getBooleanExtra(EXTRA_FIXED, false)
            if (!fixed && points.size < 2) error("Faltan puntos de la ruta")
            cleanupProviders()
            cursor = RouteCursor(points)
            val stops = mutableListOf<DwellStop>()
            val rawStops = org.json.JSONArray(intent.getStringExtra(EXTRA_STOPS) ?: "[]")
            for (i in 0 until rawStops.length()) {
                val item = rawStops.getJSONArray(i)
                stops.add(DwellStop(item.getDouble(0), item.getInt(1)))
            }
            playback = RoutePlayback(points, stops, kmh)
            paused = false
            lastUpdate = SystemClock.elapsedRealtime()
            // Android 14+ exige iniciar el servicio mientras la Activity está visible
            val note = notification("Simulando ubicación")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(ID, note, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else startForeground(ID, note)

            registerProvider(LocationManager.GPS_PROVIDER)
            // Algunos equipos carecen de NETWORK_PROVIDER: la simulación GPS sigue funcionando.
            try { registerProvider(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { }
            if (LocationManager.GPS_PROVIDER !in registeredProviders) error("No se pudo crear el proveedor GPS")
            handler.removeCallbacks(tick)
            tick.run()
        } catch (ex: Exception) {
            report("Error: ${ex.message ?: "no se pudo iniciar"}", running = false)
            stopSelf()
        }
    }

    @Suppress("DEPRECATION")
    private fun registerProvider(name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val properties = ProviderProperties.Builder()
                .setAccuracy(if (name == LocationManager.GPS_PROVIDER) ProviderProperties.ACCURACY_FINE else ProviderProperties.ACCURACY_COARSE)
                .setPowerUsage(ProviderProperties.POWER_USAGE_LOW)
                .build()
            locationManager.addTestProvider(name, properties)
        } else {
            locationManager.addTestProvider(
                name, name == LocationManager.NETWORK_PROVIDER,
                name == LocationManager.GPS_PROVIDER, false, false,
                true, true, true, Criteria.POWER_LOW,
                if (name == LocationManager.GPS_PROVIDER) Criteria.ACCURACY_FINE else Criteria.ACCURACY_COARSE
            )
        }
        registeredProviders.add(name)
        locationManager.setTestProviderEnabled(name, true)
    }

    private val tick = object : Runnable {
        override fun run() {
            val currentCursor = cursor ?: return
            val player = playback ?: return
            val now = SystemClock.elapsedRealtime()
            val dt = (now - lastUpdate).coerceIn(0, 3000)
            lastUpdate = now
            if (!paused) {
                val frame = if (fixed) PlaybackFrame(currentCursor.at(0.0), 0.0, false)
                            else player.advance(dt)
                try {
                    for (provider in registeredProviders) {
                        val location = Location(provider).apply {
                            latitude = frame.sample.point.lat
                            longitude = frame.sample.point.lon
                            accuracy = if (provider == LocationManager.GPS_PROVIDER) 3f else 10f
                            altitude = 10.0
                            speed = if (fixed || frame.waitSeconds > 0) 0f else (kmh / 3.6).toFloat()
                            bearing = frame.sample.bearing
                            time = System.currentTimeMillis()
                            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                        }
                        locationManager.setTestProviderLocation(provider, location)
                    }
                } catch (ex: Exception) {
                    report("Error de ubicación: ${ex.message}", running = false)
                    stopSelf()
                    return
                }
                val message = when {
                    fixed -> "Ubicación fija activa"
                    frame.waitSeconds > 0 -> "Parada: ${kotlin.math.ceil(frame.waitSeconds).toInt()} s restantes"
                    else -> "Recorriendo: ${String.format(Locale.US, "%.0f", frame.sample.progress * 100)}% · ${kmh.toInt()} km/h"
                }
                report(message, point = frame.sample.point)
                if (!fixed && frame.finished) {
                    report("Ruta finalizada", point = frame.sample.point, running = false)
                    stopSelf()
                    return
                }
            }
            handler.postDelayed(this, 1000L)
        }
    }

    private fun report(message: String, point: RoutePoint? = null, running: Boolean = true) {
        val status = Intent(ACTION_STATUS).setPackage(packageName)
            .putExtra("message", message)
            .putExtra("running", running)
            .putExtra("paused", paused)
        if (point != null) {
            status.putExtra("latitude", point.lat)
            status.putExtra("longitude", point.lon)
        }
        sendBroadcast(status)
        getSharedPreferences("estado", MODE_PRIVATE).edit()
            .putString("message", message)
            .putBoolean("running", running)
            .putBoolean("paused", paused)
            .apply()
    }

    private fun notification(message: String): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, MockLocationService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Ruta GPS Libre")
            .setContentText(message)
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Detener", stop).build())
            .build()
    }

    private fun updateNotification(message: String) {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(ID, notification(message))
    }

    private fun cleanupProviders() {
        registeredProviders.toList().forEach {
            try { locationManager.removeTestProvider(it) } catch (_: Exception) { }
        }
        registeredProviders.clear()
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        cursor = null
        playback = null
        cleanupProviders()
        if (getSharedPreferences("estado", MODE_PRIVATE).getBoolean("running", false)) {
            report("Servicio finalizado", running = false)
        }
        super.onDestroy()
    }
}
