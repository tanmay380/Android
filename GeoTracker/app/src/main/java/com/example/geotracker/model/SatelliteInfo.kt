package com.example.geotracker.model

data class SatelliteInfo(
    val total: Int = 0,
    val usedInFix: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
