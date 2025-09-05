package com.example.geotracker.repository

import com.example.geotracker.dao.LocationDao
import com.example.geotracker.model.LocationEntity
import kotlinx.coroutines.flow.Flow

class LocationRepository(private val dao: LocationDao) {
    suspend fun insert(loc: LocationEntity) = dao.insert(loc)
    fun getSessionLocations(sessionId: Long): Flow<List<LocationEntity>> = dao.getSessionLocations(sessionId)
}
