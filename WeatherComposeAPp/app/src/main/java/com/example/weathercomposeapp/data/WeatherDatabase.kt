package com.example.weathercomposeapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weathercomposeapp.model.Favorites

@Database(entities = [Favorites::class], version = 1, exportSchema = true)
abstract class WeatherDatabase: RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}