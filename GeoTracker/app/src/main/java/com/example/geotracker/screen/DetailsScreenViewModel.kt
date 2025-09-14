package com.example.geotracker.screen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsScreenViewModel @Inject constructor (savedStateHandle: SavedStateHandle): ViewModel() {
    init {
        Log.d("Tanmay", "inti view model: ${savedStateHandle.get<Set<Long>>("selectedId")}")
    }
}