package com.example.geotracker.screen.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.helper.computeDistanceKm
import com.example.geotracker.model.SessionDetailedSummary
import com.example.geotracker.repository.LocationRepository
import com.example.geotracker.repository.SelectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, private val locationRepository: LocationRepository,
    private val selectionManager: SelectionManager
) : ViewModel() {

    private val _selectedSession = MutableStateFlow<List<SessionDetailedSummary>>(emptyList())
    val selectedSession = _selectedSession.asStateFlow()

    init {
        viewModelScope.launch {

            selectionManager.selectedSessionId
                .distinctUntilChanged { old, new -> old.size == new.size }
                .collectLatest {
                    updateSessionsBySelectedIds(it)
                    Log.d(TAG, "selected sessions ids: ${_selectedSession.value}")
                }
        }
    }

    suspend fun updateSessionsBySelectedIds(ids: Set<Long>) {
//        _selectedSession.value = emptyList()
        val tempList = mutableListOf<SessionDetailedSummary>()
        ids.map {
            sessionDetailedSummary(it)?.let { tempList.add(it) }
        }
        _selectedSession.value = tempList
    }

    private suspend fun sessionDetailedSummary(it: Long): SessionDetailedSummary? {
        val start =
            locationRepository.getSessionStartTime(it) ?: return null
        val end = locationRepository.getSessionEndTime(it) ?: start
        val points = locationRepository.getSessionLocationsList(it)
        val distanceKm = computeDistanceKm(points)
        val duration = (end - start).coerceAtLeast(0L)

        val session = SessionDetailedSummary(
            sessionId = it,
            startTimeMs = start,
            endTimeMs = end,
            durationMs = duration,
            distanceKm = distanceKm,
            speed = 0.0,
            avgSpeed = 0.0,
            maxSpeed = 0.0
        )

        return session

    }


    fun openSessionById(sessionId: Long) {

    }

}