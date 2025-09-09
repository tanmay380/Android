package com.example.geotracker.helper

import android.util.Log
import com.example.geotracker.model.RoutePoint
import kotlin.math.pow

// returns meters
fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2).pow(2.0) +
            kotlin.math.cos(Math.toRadians(lat1)) *
            kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2).pow(2.0)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return R * c
}

fun findNearestRoutePointIndex(route: List<RoutePoint>, lat: Double, lng: Double): Pair<Int, Double> {
    if (route.isEmpty()) return -1 to Double.MAX_VALUE
    var bestIndex = 0
    var bestDist = Double.MAX_VALUE
    for (i in route.indices) {
        val p = route[i]
        val d = haversineMeters(lat, lng, p.lat, p.lng)
        if (d < bestDist) {
            bestDist = d
            bestIndex = i
        }
    }
    Log.d("tanmay", "findNearestRoutePointIndex: $bestDist")
    return bestIndex to bestDist
}
