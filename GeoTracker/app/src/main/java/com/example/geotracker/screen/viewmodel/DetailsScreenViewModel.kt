package com.example.geotracker.screen.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.helper.computeDistanceKm
import com.example.geotracker.location.service.LocationService
import com.example.geotracker.model.SessionDetailedSummary
import com.example.geotracker.model.SessionSummary
import com.example.geotracker.repository.LocationRepository
import com.example.geotracker.repository.SelectionManager
import com.example.geotracker.utils.Constants.PREFS
import com.example.geotracker.utils.Constants.PREF_ACTIVE_SESSION
import com.example.geotracker.utils.Utils.formatDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class DetailsScreenViewModel @Inject constructor(
    private val context: Application,
    private val locationRepository: LocationRepository,
    private val selectionManager: SelectionManager
) : ViewModel() {

    val serviceRunningFlow = LocationService.isServiceRunning.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), false
    )

    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val sidFromPrefs = prefs.getLong(PREF_ACTIVE_SESSION, -1L)

    val currentServiceSessionIdFlow = MutableStateFlow<Long>(sidFromPrefs)

    private val _selectedSession = MutableStateFlow<MutableList<SessionDetailedSummary>>(mutableListOf())
    val selectedSession = _selectedSession.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val sessionSummaryState: StateFlow<List<SessionDetailedSummary>> =
        combine(serviceRunningFlow, currentServiceSessionIdFlow) { running, sid -> running to sid }
            .flatMapLatest { (running, sid) ->
                if (!running || sid == null) {
                    // service not running or no id -> emit empty list
                    flowOf(emptyList())
                } else {
                    // service running for sid -> subscribe to DB flow for that session
                    locationRepository.getSessionLocations(sid) // Flow<List<LocationEntity>>
                        .mapLatest { points ->
                            // mapLatest is suspendable; you can call suspend funcs if needed
                            val start = locationRepository.getSessionStartTime(sid) ?: points.firstOrNull()?.timestamp ?: return@mapLatest null
                            val end = locationRepository.getSessionEndTime(sid) ?: start
                            val distanceKm = computeDistanceKm(points)
                            val duration = (if (end > start) end else System.currentTimeMillis()) - start
                            SessionDetailedSummary(
                                sessionId = sid,
                                startTimeMs = start,
                                endTimeMs = end,
                                durationMs = duration,
                                distanceKm = distanceKm,
                                speed = points.lastOrNull()?.speed?.times(3.6f)?.toDouble() ?: 0.0,
                                avgSpeed = 0.0,
                                maxSpeed = 0.0
                            )
                        }
                        .filterNotNull()
                        .map {
                            listOf(it)
                        } // convert single summary into a List<SessionDetailedSummary>
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    init {
        viewModelScope.launch {
            selectionManager.selectedSessionId
                .distinctUntilChanged { old, new -> old.size == new.size }
                .collectLatest {
                    updateSessionsBySelectedIds(it)
                    Log.d(TAG, "selected sessions ids: ${_selectedSession.value}")
                }
        }
        viewModelScope.launch {
            currentServiceSessionIdFlow
                .flatMapLatest { sid ->
                    if (sid == null) flowOf(emptyList())
                    else {
                        locationRepository.getSessionLocations(sid)
                            .mapLatest { points ->
                                val start = locationRepository.getSessionStartTime(sid) ?: return@mapLatest null
                                val end = locationRepository.getSessionEndTime(sid) ?: start
                                val distanceKm = computeDistanceKm(points)
                                val duration = (end - start).coerceAtLeast(0L)

                                SessionDetailedSummary(
                                    sessionId = sid,
                                    startTimeMs = start,
                                    endTimeMs = end,
                                    durationMs = duration,
                                    distanceKm = distanceKm,
                                    speed = points.lastOrNull()?.speed?.times(3.6f)?.toDouble() ?: 0.0,
                                    avgSpeed = 0.0,
                                    maxSpeed = 0.0
                                )
                            }
                            .filterNotNull()
                            .map { listOf(it) } // Flow<List<SessionDetailedSummary>>
                    }
                }
                .collectLatest { list ->
                    if (list.isNotEmpty()) _selectedSession.value[0] = list[0]
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