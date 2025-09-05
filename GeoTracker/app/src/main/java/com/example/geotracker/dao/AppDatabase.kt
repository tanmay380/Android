package com.example.geotracker.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.geotracker.model.LocationEntity

@Database(entities = [LocationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}
