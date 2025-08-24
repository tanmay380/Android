package com.example.weathercomposeapp.network

import com.example.weathercomposeapp.model.Location
import com.example.weathercomposeapp.model.Weather
import com.example.weathercomposeapp.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Singleton

interface WeatherApi {
    @GET(value = "forecast.json?key=${Constants.API_KEY}")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("days") days: Int,
        @Query("aqi") aqi: String,
    ) : Weather
//    @GET(value = "forecast.json?key=9bc506671c534951974152654252308&q=London")
//    suspend fun getWeather() : Weather
}