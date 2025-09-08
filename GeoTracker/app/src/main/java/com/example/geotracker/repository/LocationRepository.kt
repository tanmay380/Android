package com.example.geotracker.repository

import android.util.Log
import com.example.geotracker.components.SessionSummary
import com.example.geotracker.dao.LocationDao
import com.example.geotracker.model.LocationEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class LocationRepository @Inject constructor(private val dao: LocationDao) {
    suspend fun insert(loc: LocationEntity) = dao.insert(loc)
    fun getSessionLocations(sessionId: Long): Flow<List<LocationEntity>> = dao.getSessionLocations(sessionId)
    suspend fun deleteSession(sessionId: Long) = dao.deleteSession(sessionId)

    // stream of summaries: whenever session ids change, recompute summaries
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllSessionSummariesFlow(): Flow<List<SessionSummary>> {
        return dao.getAllSessionIdsFlow().flatMapLatest { ids ->
            // map each id into summary by suspending DB calls
            flow {
                val summaries = ids.mapNotNull { id ->
                    // compute start/end quickly (will be null if db race)
                    val start = dao.getSessionStartTime(id) ?: return@mapNotNull null
                    val end = dao.getSessionEndTime(id) ?: start
                    val points = dao.getSessionLocationsList(id) // suspend
                    val distanceKm = computeDistanceKm(points)
                    val duration = (end - start).coerceAtLeast(0L)
                    SessionSummary(
                        sessionId = id,
                        startTimeMs = start,
                        endTimeMs = end,
                        durationMs = duration,
                        distanceKm = distanceKm
                    )
                }
                emit(summaries)
            }
        }
    }

    private fun computeDistanceKm(points: List<LocationEntity>): Double {
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

}
