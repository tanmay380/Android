package com.example.geotracker.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.geotracker.model.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(loc: LocationEntity)

    @Query("SELECT * FROM locations WHERE sessionId = :id ORDER BY timestamp ASC")
    fun getSessionLocations(id: Long): Flow<List<LocationEntity>>

    // new: get all session ids as a Flow so we can react to DB updates
    @Query("SELECT DISTINCT sessionId FROM locations ORDER BY sessionId DESC")
    fun getAllSessionIdsFlow(): Flow<List<Long>>

    // new: one-shot list (suspend) used to compute aggregates
    @Query("SELECT * FROM locations WHERE sessionId = :id ORDER BY timestamp ASC")
    suspend fun getSessionLocationsList(id: Long): List<LocationEntity>

    // optional helper to get earliest/latest timestamps for a session (fast)
    @Query("SELECT MIN(timestamp) FROM locations WHERE sessionId = :id")
    suspend fun getSessionStartTime(id: Long): Long?

    @Query("SELECT MAX(timestamp) FROM locations WHERE sessionId = :id")
    suspend fun getSessionEndTime(id: Long): Long?
}
