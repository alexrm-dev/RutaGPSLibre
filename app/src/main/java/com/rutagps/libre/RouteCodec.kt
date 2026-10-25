package com.rutagps.libre

import org.json.JSONArray

object RouteCodec {
    fun encode(points: List<RoutePoint>): String {
        val arr = JSONArray()
        points.forEach { arr.put(JSONArray().put(it.lat).put(it.lon)) }
        return arr.toString()
    }
    fun decode(json: String): List<RoutePoint> {
        val arr = JSONArray(json)
        require(arr.length() in 1..3000) { "Cantidad de coordenadas no válida" }
        return (0 until arr.length()).map {
            val pair = arr.getJSONArray(it)
            RoutePoint(pair.getDouble(0), pair.getDouble(1))
        }
    }
}
