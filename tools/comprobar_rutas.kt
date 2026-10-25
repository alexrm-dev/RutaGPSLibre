import com.rutagps.libre.*
import kotlin.math.abs
fun main() {
    val lima = RoutePoint(-12.0464, -77.0428)
    val other = RoutePoint(-12.0464, -77.0328)
    val distance = RouteMath.distanceMeters(lima,other)
    check(distance in 1000.0..1200.0)
    val cursor = RouteCursor(listOf(lima,other))
    check(abs(cursor.at(distance/2).point.lon + 77.0378)<1e-5)
    check(cursor.at(1e8).point == other)
    check(RouteCursor(listOf(lima,lima,other)).at(10.0).point.lat == lima.lat)
    check(RouteCursor(listOf(lima)).at(200.0).point == lima)
    println("OK: 5 pruebas básicas de cálculo de ruta")
}
