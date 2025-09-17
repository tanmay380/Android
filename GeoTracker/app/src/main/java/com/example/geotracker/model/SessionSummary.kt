package com.example.geotracker.model

data class SessionSummary(
    val sessionId: Long,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    val distanceKm: Double
)
data class SessionDetailedSummary(
    val sessionId: Long,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    val distanceKm: Double,
    val speed: Double,
    val avgSpeed: Double,
    val maxSpeed: Double
)
