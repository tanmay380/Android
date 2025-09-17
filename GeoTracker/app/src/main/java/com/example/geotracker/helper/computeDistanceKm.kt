package com.example.geotracker.helper

import com.example.geotracker.model.LocationEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun computeDistanceKm(points: List<LocationEntity>): Double {
        if (points.size < 2) return 0.0
        var totalMeters = 0.0
        for (i in 1 until points.size) {
            val a = points[i - 1]
            val b = points[i]
            totalMeters += distanceBetween(a.lat, a.lng, b.lat, b.lng)
        }
        return totalMeters / 1000.0
    }

    // Haversine distance (meters)
    private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }