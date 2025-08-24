package com.example.weathercomposeapp.screens.main

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weathercomposeapp.data.DataOrException
import com.example.weathercomposeapp.model.Location
import com.example.weathercomposeapp.model.Weather
import com.example.weathercomposeapp.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val weatherRepository: WeatherRepository) : ViewModel(){

    suspend fun getWeatherData(city: String):
    DataOrException<Weather, Boolean, Exception> {
        return weatherRepository.getWeather(city)
    }
}