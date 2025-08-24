package com.example.weathercomposeapp.model

import com.google.gson.annotations.SerializedName

data class AirQualityX(
    val co: Double,
    @SerializedName("gb-defra-index")
    val gbdefraindex: Int,
    val no2: Double,
    val o3: Int,
    val pm10: Double,
    val pm2_5: Double,
    val so2: Double,
    @SerializedName("us-epa-index")
    val usepaindex: Int
)