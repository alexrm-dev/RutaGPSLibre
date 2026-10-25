package com.rutagps.libre

import kotlin.math.*

/** Núcleo independiente de Android: coordenadas, distancia y avance a velocidad constante. */
data class RoutePoint(val lat: Double, val lon: Double) {
    init { require(lat in -90.0..90.0 && lon in -180.0..180.0) }
}

data class RouteSample(val point: RoutePoint, val bearing: Float, val progress: Double)

object RouteMath {
    private const val R = 6371000.0

    fun distanceMeters(a: RoutePoint, b: RoutePoint): Double {
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(b.lon - a.lon)
        val x = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
        return 2 * R * atan2(sqrt(x), sqrt(max(0.0, 1 - x)))
    }

    fun bearing(a: RoutePoint, b: RoutePoint): Float {
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val dLon = Math.toRadians(b.lon - a.lon)
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
    }

    fun interpolate(a: RoutePoint, b: RoutePoint, fraction: Double): RoutePoint {
        val t = fraction.coerceIn(0.0, 1.0)
        // Adecuado para segmentos urbanos cortos, no para recorridos transoceánicos.
        return RoutePoint(a.lat + (b.lat - a.lat) * t, a.lon + (b.lon - a.lon) * t)
    }
}

class RouteCursor(points: List<RoutePoint>) {
    val path: List<RoutePoint> = points.toList()
    private val cumulative: DoubleArray
    val lengthMeters: Double

    init {
        require(path.isNotEmpty()) { "La ruta no puede estar vacía" }
        cumulative = DoubleArray(path.size)
        for (i in 1 until path.size) {
            cumulative[i] = cumulative[i - 1] + RouteMath.distanceMeters(path[i - 1], path[i])
        }
        lengthMeters = cumulative.last()
    }

    fun distanceAtIndex(index: Int): Double = cumulative[index.coerceIn(0, cumulative.lastIndex)]

    fun at(meters: Double): RouteSample {
        if (path.size == 1 || lengthMeters == 0.0) return RouteSample(path.first(), 0f, 1.0)
        val travelled = meters.coerceIn(0.0, lengthMeters)
        var lo = 1
        var hi = path.lastIndex
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (cumulative[mid] < travelled) lo = mid + 1 else hi = mid
        }
        val segment = lo
        val start = path[segment - 1]
        val end = path[segment]
        val segmentLength = cumulative[segment] - cumulative[segment - 1]
        val f = if (segmentLength < 0.00001) 1.0 else (travelled - cumulative[segment - 1]) / segmentLength
        return RouteSample(RouteMath.interpolate(start, end, f), RouteMath.bearing(start, end), travelled / lengthMeters)
    }
}
