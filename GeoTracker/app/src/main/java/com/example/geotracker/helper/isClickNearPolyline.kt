import android.graphics.Point
import com.example.geotracker.model.RoutePoint
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

// squared distance from P to segment AB (Points)
private fun pointToSegmentDistSq(p: Point, a: Point, b: Point): Float {
    val px = p.x.toFloat(); val py = p.y.toFloat()
    val ax = a.x.toFloat(); val ay = a.y.toFloat()
    val bx = b.x.toFloat(); val by = b.y.toFloat()

    val vx = bx - ax; val vy = by - ay
    val wx = px - ax; val wy = py - ay

    val c1 = vx * wx + vy * wy
    if (c1 <= 0f) return (px - ax).pow(2) + (py - ay).pow(2)
    val c2 = vx * vx + vy * vy
    if (c2 <= c1) return (px - bx).pow(2) + (py - by).pow(2)
    val t = c1 / c2
    val projx = ax + t * vx
    val projy = ay + t * vy
    return (px - projx).pow(2) + (py - projy).pow(2)
}

// min pixel distance from click to a single polyline
fun minPixelDistanceToPolyline(clickLatLng: LatLng, polyline: List<RoutePoint>, projection: Projection): Pair<Float, Int> {
    if (polyline.size < 2) return Float.MAX_VALUE to -1
    val clickPt = projection.toScreenLocation(clickLatLng)
    var minD2 = Float.MAX_VALUE
    var bestIndex: Int = -1
    for (i in 0 until polyline.size - 1) {
        val a = projection.toScreenLocation(LatLng(polyline[i].lat, polyline[i].lng))
        val b = projection.toScreenLocation(LatLng(polyline[i+1].lat, polyline[i+1].lng))
        val d2 = pointToSegmentDistSq(clickPt, a, b)
        if (d2 < minD2) {
            minD2 = d2
            bestIndex = i
        }
    }
    return sqrt(minD2.toDouble()).toFloat() to bestIndex
}

// meters per pixel approx (used to convert px -> meters)
fun metersPerPixel(lat: Double, zoom: Double): Double {
    val latRad = Math.toRadians(lat)
    return 156543.03392804062 * cos(latRad) / (2.0.pow(zoom))
}

// call from map click listener
