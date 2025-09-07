package com.example.geotracker.components

import java.sql.Time

//data class SessionSummary(
//    val id: Long,
//    val title: String,
//    val distance: Int,
//    val duration: Time,
//    val timeAgo: String
//)

data class SessionSummary(
    val sessionId: Long,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationMs: Long,
    val distanceKm: Double
)
