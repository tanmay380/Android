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
}
