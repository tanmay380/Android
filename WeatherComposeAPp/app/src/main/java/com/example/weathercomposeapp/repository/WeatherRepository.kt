package com.example.weathercomposeapp.repository

import android.util.Log
import com.example.weathercomposeapp.data.DataOrException
import com.example.weathercomposeapp.model.Location
import com.example.weathercomposeapp.model.Weather
import com.example.weathercomposeapp.network.WeatherApi
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val weatherApi: WeatherApi) {

    suspend fun getWeather(cityQuery : String): DataOrException<Weather, Boolean, Exception> {
        val response = try {
            weatherApi.getWeather(cityQuery, 7, "yes")
        }catch (e: Exception){
            Log.d("tanmay", "getWeather: $e")
            return DataOrException(exception = e)
        }
        Log.d("tanmay", "getWeather: ${response.location.name}")
        return DataOrException(data = response, boolean = false)
    }

}