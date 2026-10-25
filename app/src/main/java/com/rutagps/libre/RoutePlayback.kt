package com.rutagps.libre

import kotlin.math.min

/** Espera en un punto intermedio: distancia en metros desde el inicio y tiempo en segundos. */
data class DwellStop(val atMeters: Double, val seconds: Int)

/** Calcula la parada más próxima a cada punto intermedio sobre el recorrido preparado. */
object StopPlanner {
    fun fromWaypoints(
        route: List<RoutePoint>, waypoints: List<RoutePoint>, dwellSeconds: List<Int>
    ): List<DwellStop> {
        if (route.size < 2 || waypoints.size < 3) return emptyList()
        val cursor = RouteCursor(route)
        var fromIndex = 0
        val out = mutableListOf<DwellStop>()
        for (i in 1 until waypoints.lastIndex) {
            val seconds = dwellSeconds.getOrElse(i) { 0 }.coerceIn(0, 3600)
            if (seconds == 0) continue
            var nearest = fromIndex
            var closest = Double.POSITIVE_INFINITY
            for (j in fromIndex until route.lastIndex) {
                val d = RouteMath.distanceMeters(waypoints[i], route[j])
                if (d < closest) { closest = d; nearest = j }
            }
            fromIndex = nearest
            val distance = cursor.distanceAtIndex(nearest)
            if (distance < cursor.lengthMeters) out.add(DwellStop(distance, seconds))
        }
        return out.sortedBy { it.atMeters }
    }
}

data class PlaybackFrame(
    val sample: RouteSample,
    val waitSeconds: Double,
    val finished: Boolean
)

/** Reloj determinista independiente de Android. Nunca necesita geolocalización real. */
class RoutePlayback(
    points: List<RoutePoint>,
    stops: List<DwellStop>,
    initialKmh: Double
) {
    private val cursor = RouteCursor(points)
    private val schedule = stops.filter { it.seconds > 0 && it.atMeters >= 0 && it.atMeters < cursor.lengthMeters }
        .sortedBy { it.atMeters }
    var speedKmh: Double = initialKmh.coerceIn(1.0, 120.0)
        set(value) { field = value.coerceIn(1.0, 120.0) }
    var meters = 0.0
        private set
    private var index = 0
    private var wait = 0.0
    val lengthMeters: Double get() = cursor.lengthMeters

    fun current(): PlaybackFrame = PlaybackFrame(cursor.at(meters), wait, meters >= lengthMeters)

    fun advance(milliseconds: Long): PlaybackFrame {
        var remaining = (milliseconds.coerceIn(0, 10000) / 1000.0)
        var safety = 0
        while (remaining > 0.000001 && meters < lengthMeters && safety++ < schedule.size * 2 + 4) {
            if (wait > 0.0) {
                val used = min(wait, remaining)
                wait -= used
                remaining -= used
                continue
            }
            if (index < schedule.size && meters >= schedule[index].atMeters - 0.00001) {
                wait = schedule[index].seconds.toDouble()
                index++
                continue
            }
            val nextDistance = if (index < schedule.size) schedule[index].atMeters else lengthMeters
            val metersPerSecond = speedKmh / 3.6
            val motion = min(remaining * metersPerSecond, (nextDistance - meters).coerceAtLeast(0.0))
            meters = (meters + motion).coerceAtMost(lengthMeters)
            remaining -= motion / metersPerSecond
        }
        return current()
    }
}
