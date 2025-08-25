package com.example.weathercomposeapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.annotation.Nonnull

@Entity(tableName = "fav_tbl")
data class Favorites(
    @Nonnull
    @PrimaryKey
    val city: String,
    val country: String,
)
