package com.example.geotracker.repository

import android.util.Log
import com.example.geotracker.dao.LocationDao
import com.example.geotracker.helper.computeDistanceKm
import com.example.geotracker.model.LocationEntity
import com.example.geotracker.model.SessionSummary
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
    suspend fun getSessionStartTime(sessionId: Long): Long? = dao.getSessionStartTime(sessionId)
    suspend fun getSessionEndTime(sessionId: Long): Long? = dao.getSessionEndTime(sessionId)
    suspend fun getSessionLocationsList (sessionId: Long): List<LocationEntity> = dao.getSessionLocationsList(sessionId)
    fun getAllSessionIdsFlow(): Flow<List<Long>> = dao.getAllSessionIdsFlow()

    // stream of summaries: whenever session ids change, recompute summaries
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllSessionSummariesFlow(): Flow<List<SessionSummary>> {
        return dao.getAllSessionIdsFlow().flatMapLatest { ids ->
            // map each id into summary by suspending DB calls
            flow {
                val summaries = ids.mapNotNull { id ->
                    // compute start/end quickly (will be null if db race)
                    val start = getSessionStartTime(id) ?: return@mapNotNull null
                    val end = getSessionEndTime(id) ?: start
                    val points = getSessionLocationsList(id) // suspend
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

}
