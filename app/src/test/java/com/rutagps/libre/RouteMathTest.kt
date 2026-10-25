package com.rutagps.libre

import org.junit.Assert.*
import org.junit.Test

class RouteMathTest {
    @Test fun distanceAndInterpolation() {
        val a = RoutePoint(-12.0464, -77.0428)
        val b = RoutePoint(-12.0464, -77.0328)
        val d = RouteMath.distanceMeters(a, b)
        assertTrue(d > 1000 && d < 1200)
        val route = RouteCursor(listOf(a, b))
        val middle = route.at(d / 2)
        assertEquals(-77.0378, middle.point.lon, 0.00001)
        assertEquals(0.5, middle.progress, 0.0001)
    }
    @Test fun endpointIsClamped() {
        val a = RoutePoint(0.0, 0.0)
        val b = RoutePoint(0.0, 0.01)
        val route = RouteCursor(listOf(a, b))
        assertEquals(a, route.at(-1.0).point)
        assertEquals(b, route.at(1e9).point)
    }
    @Test fun duplicateCoordinatesDoNotCrash() {
        val a = RoutePoint(1.0, 2.0)
        val b = RoutePoint(1.001, 2.0)
        val route = RouteCursor(listOf(a, a, b))
        assertTrue(route.at(10.0).point.lat >= 1.0)
    }
}
