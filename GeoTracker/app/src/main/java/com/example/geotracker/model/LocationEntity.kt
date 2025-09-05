package com.example.geotracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Long,
    val lat: Double,
    val lng: Double,
    val speed: Float,
    val timestamp: Long
){
}
