import com.rutagps.libre.*
import kotlin.math.abs

fun close(a: Double, b: Double, tolerance: Double = 0.01) = check(abs(a - b) < tolerance) { "$a != $b" }
fun main() {
    val a = RoutePoint(-12.0, -77.0)
    val b = RoutePoint(-12.0, -76.995)
    val c = RoutePoint(-12.0, -76.99)
    val path = listOf(a, b, c)
    val stops = StopPlanner.fromWaypoints(path, path, listOf(0, 3, 0))
    check(stops.size == 1 && stops[0].seconds == 3)
    val player = RoutePlayback(path, stops, 36.0)
    player.advance(1000)
    close(player.meters, 10.0)
    player.speedKmh = 72.0
    player.advance(1000)
    close(player.meters, 30.0)
    val metersBeforeStop = stops.single().atMeters
    while (player.meters < metersBeforeStop - 5.0) player.advance(200)
    var frame = player.current()
    while (frame.waitSeconds <= 0.0) frame = player.advance(200)
    close(player.meters, metersBeforeStop, 1.0)
    check(frame.waitSeconds > 0) { "Debe esperar en la parada" }
    player.advance(2000)
    close(player.meters, metersBeforeStop, 1.0)
    player.advance(10000)
    check(player.meters > metersBeforeStop) { "Debe continuar tras la espera" }
    while (!player.current().finished) player.advance(10000)
    close(player.meters, RouteCursor(path).lengthMeters, 1.0)
    check(StopPlanner.fromWaypoints(listOf(a,b), listOf(a,b), listOf(0,0)).isEmpty())
    println("PRUEBAS OK: paradas, velocidad dinámica, reanudación y destino")
}
