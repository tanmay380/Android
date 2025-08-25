package com.example.weathercomposeapp.repository

import com.example.weathercomposeapp.data.WeatherDao
import com.example.weathercomposeapp.model.Favorites
import dagger.Module
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class WeatherDataRepository @Inject constructor(private val weatherDao: WeatherDao){

    fun getFavorites(): Flow<List<Favorites>> = weatherDao.getFavorites()

    suspend fun insertFavorites(favorites: Favorites)= weatherDao.saveFavorites(favorites)

    suspend fun deleteAllFavorites() = weatherDao.deleteAllFav()

    suspend fun deleteFavCity(favorites: Favorites) = weatherDao.deleteFavCity(favorites)

    suspend fun getFavByCity(city: String): Favorites = weatherDao.getFavById(city)

}