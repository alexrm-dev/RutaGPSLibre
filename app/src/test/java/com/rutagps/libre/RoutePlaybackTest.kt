package com.rutagps.libre

import org.junit.Assert.*
import org.junit.Test

class RoutePlaybackTest {
    private val a = RoutePoint(-12.0, -77.0)
    private val b = RoutePoint(-12.0, -76.995)
    private val c = RoutePoint(-12.0, -76.99)

    @Test fun favoriteStopIsMappedToIntermediatePoint() {
        val stops = StopPlanner.fromWaypoints(listOf(a,b,c), listOf(a,b,c), listOf(0,5,0))
        assertEquals(1, stops.size)
        assertEquals(5, stops.single().seconds)
        assertEquals(RouteCursor(listOf(a,b,c)).distanceAtIndex(1), stops.single().atMeters, 1.0)
    }

    @Test fun changingSpeedUpdatesDistance() {
        val playback = RoutePlayback(listOf(a,b,c), emptyList(), 36.0)
        playback.advance(1000)
        assertEquals(10.0, playback.meters, 0.1)
        playback.speedKmh = 72.0
        playback.advance(1000)
        assertEquals(30.0, playback.meters, 0.1)
    }

    @Test fun stopWaitsThenResumesAndFinishes() {
        val path = listOf(a,b,c)
        val dwell = StopPlanner.fromWaypoints(path, path, listOf(0,3,0))
        val playback = RoutePlayback(path, dwell, 72.0)
        var loops = 0
        while (playback.current().waitSeconds <= 0.0 && loops++ < 500) playback.advance(200)
        assertTrue(playback.current().waitSeconds > 0.0)
        val atStop = playback.meters
        playback.advance(1000)
        assertEquals(atStop, playback.meters, 0.01)
        playback.advance(10000)
        assertTrue(playback.meters > atStop)
        loops = 0
        while (!playback.current().finished && loops++ < 2000) playback.advance(10000)
        assertTrue(playback.current().finished)
    }
}
