package com.example.weathercomposeapp.screens.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathercomposeapp.model.Favorites
import com.example.weathercomposeapp.repository.WeatherDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repository: WeatherDataRepository) :
    ViewModel() {

    private val _favList = MutableStateFlow<List<Favorites>>(emptyList())
    val favList = _favList.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavorites().distinctUntilChanged()
                .collect {
                        _favList.value = it

                        Log.d("tanmay", "list is not empty: ${favList.value}")
                    }
                }

    }

    fun insertFavorite(favorites: Favorites) = viewModelScope.launch {
        repository.insertFavorites(favorites)
        Log.d("tanmay", "insertFavorite: ${favorites.country}  ${favorites.city}")
    }

    fun deleteFavorite(favorites: Favorites) = viewModelScope.launch {
        repository.deleteFavCity(favorites)
    }

    fun checkFavorite(favorites: Favorites): (Boolean) {
        val favCity = viewModelScope.launch {
            repository.getFavByCity(favorites.city)
        }
        return favCity==null
    }

}