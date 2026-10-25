package com.rutagps.libre

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/** Servicio OSRM público: prototipos personales, sin garantía ni uso intensivo. */
object RoadRouting {
    fun calculate(waypoints: List<RoutePoint>): List<RoutePoint> {
        require(waypoints.size in 2..15) { "Selecciona entre 2 y 15 puntos" }
        val coords = waypoints.joinToString(";") {
            String.format(Locale.US, "%.6f,%.6f", it.lon, it.lat)
        }
        val endpoint = "https://router.project-osrm.org/route/v1/driving/$coords?overview=full&geometries=geojson&steps=false"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 12000
            connection.readTimeout = 18000
            connection.setRequestProperty("User-Agent", "RutaGPSLibre/0.1 (Android; educational prototype)")
            if (connection.responseCode != 200) error("Servidor de rutas no disponible: ${connection.responseCode}")
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            if (json.optString("code") != "Ok") error("No hay ruta para los puntos elegidos")
            val coordsJson = json.getJSONArray("routes").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates")
            val raw = (0 until coordsJson.length()).map { i ->
                val c = coordsJson.getJSONArray(i)
                RoutePoint(c.getDouble(1), c.getDouble(0))
            }
            if (raw.size < 2) error("El servidor devolvió una ruta vacía")
            // Evita superar el límite de tamaño al enviar la ruta al servicio.
            if (raw.size <= 2800) return raw
            val sampled = (0 until 2799).map { index ->
                raw[index * (raw.lastIndex) / 2799]
            }
            return sampled + raw.last()
        } finally {
            connection.disconnect()
        }
    }
}
