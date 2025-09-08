package com.example.geotracker.screen

import android.location.Location
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.components.SessionSummary
import com.example.geotracker.helper.LocationProvider
import com.example.geotracker.repository.LocationRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingUiState(
    val currentLatLng: LatLng = LatLng(0.0, 0.0),
    val routePoints: List<LatLng> = emptyList(),
    val distance: Double = 0.0,
    val speed: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val locationPermissionGranted: Boolean = false,
    val sessionStartMs: Long = 0L,
    val bearing: Float = 0f
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repo: LocationRepository,
    private val locationProvider: LocationProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val MIN_POLYLINE_DISTANCE_M = 2.0// inside ViewModel class
    private var lastLatLngForPolyline: LatLng? = null


    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState

    val sessionSummary: StateFlow<List<SessionSummary>> =
        repo.getAllSessionSummariesFlow().map {
            it
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    var _isServiceStarted = MutableStateFlow(false)
    var isServiceStarted: StateFlow<Boolean> = _isServiceStarted.asStateFlow()

    private var totalDistance = 0.0
    private var lastLatLng: LatLng? = null

    private var _sessionId = MutableStateFlow<Long?>(savedStateHandle["sessionId"])
    var sessionId: StateFlow<Long?> = _sessionId.asStateFlow()

    fun setSessionId(id: Long?) {
        _sessionId.value = id
        _isServiceStarted.value = true
    }

    fun setServiceStarted() {
        _uiState.update {
            it.copy(
                sessionStartMs = System.currentTimeMillis()
            )
        }
        _isServiceStarted.value = true
    }

    fun openSession(sId: Long) {
        viewModelScope.launch {
            openSessionBySessionId(sId)
        }

    }

    // backing mutable + public read-only selection flow
    private val _selectedSessionId = MutableStateFlow<Set<Long?>>(emptySet())
    val selectedSessionId: StateFlow<Set<Long?>> = _selectedSessionId.asStateFlow()

    /** Toggle selection: select id if not selected, otherwise clear selection. */
    fun toggleSessionSelection(sessionId: Long) {
        val current = _selectedSessionId.value.toMutableSet()
        if (current.contains(sessionId)) current.remove(sessionId) else current.add(sessionId)
        _selectedSessionId.value = current.toSet()
    }

    /** Optional helper to explicitly select (useful when opening session programmatically). */

    /** Optional helper to clear selection */
    fun clearSelection() {
        _selectedSessionId.value = emptySet()
    }


    fun createSessionIfAbsent() {
        if (_sessionId.value == null) {
            val id = System.currentTimeMillis()
            _sessionId.value = id
            savedStateHandle["sessionId"] = id
            _uiState.value = _uiState.value.copy(sessionStartMs = id)
        }
        clearSelection()
        _uiState.value = TrackingUiState()

        Log.d("tanmay", "createSessionIfAbsent: ${sessionId.value}")
    }

    fun startGettingLocation() {
        locationProvider.startUpdates()
        viewModelScope.launch {
            locationProvider.locFlow.collect {
                Log.d("tanmay", "startGettingLocation: ${it.bearing}")
                _uiState.value = _uiState.value.copy(
                    currentLatLng = LatLng(it.latitude, it.longitude),
                    bearing = it.bearing
                )
            }
        }
    }

    fun handleNewLocation(entity: Location) {
        locationProvider.stopUpdates()

        val newLL = LatLng(entity.latitude, entity.longitude)
        var lastAppended = lastLatLngForPolyline
        var addedToPolyline = false

        lastLatLng?.let { prev ->
            val d = Location("").apply {
                latitude = prev.latitude
                longitude = prev.longitude
            }.distanceTo(
                Location("").apply {
                    latitude = newLL.latitude
                    longitude = newLL.longitude
                }
            )
            totalDistance += d
        }
        lastLatLng = newLL

        if (lastAppended == null) {
            // first point: always append
            _uiState.update {
                it.copy(
                    currentLatLng = newLL,
                    routePoints = it.routePoints + newLL
                )
            }
            lastAppended = newLL
            lastLatLngForPolyline = newLL
            addedToPolyline = true
        } else {
            val dMeters = FloatArray(1)
            android.location.Location.distanceBetween(
                lastAppended.latitude, lastAppended.longitude,
                newLL.latitude, newLL.longitude, dMeters
            )
            if (dMeters[0] >= MIN_POLYLINE_DISTANCE_M) {
                // movement exceeds threshold -> append and increment distance
                _uiState.update { old ->
                    val addedDistance = dMeters[0].toDouble()
                    val newTotalMeters = (old.distance * 1000.0) + addedDistance
                    old.copy(
                        currentLatLng = newLL,
                        routePoints = old.routePoints + newLL,
                        distance = newTotalMeters / 1000.0
                    )
                }
                lastLatLngForPolyline = newLL
                addedToPolyline = true
            } else {
                // small move: update only current position & speed/avg but do NOT append
                _uiState.update { old ->
                    old.copy(currentLatLng = newLL, speed = entity.speed * 3.6)
                }
            }
        }
        val elapsedHours = (entity.time - (sessionId.value!!)) / 1000.0 / 3600.0
        _uiState.update { cur ->
            val totalKm = cur.distance
            val avg = if (elapsedHours > 0) totalKm / elapsedHours else cur.avgSpeed
            cur.copy(avgSpeed = avg, speed = entity.speed * 3.6, bearing = entity.bearing)
        }

//
//        val elapsedHrs = (location.timestamp - sessionId.value!!.toLong()) / 1000.0 / 3600.0
//        _uiState.value = _uiState.value.copy(
//            currentLatLng = newLL,
//            routePoints = _uiState.value.routePoints + newLL,
//            distance = totalDistance / 1000.0,
//            speed = location.speed * 3.6,
//            avgSpeed = if (elapsedHrs > 0) (totalDistance / 1000.0) / elapsedHrs else 0.0
//        )
        Log.d("tanmay", "handleNewLocation: ${_uiState.value.sessionStartMs}")


    }


    override fun onCleared() {
        super.onCleared()
    }

    // fun openSessionBySessionId(sid: Long) {
    //     val locations = repo.getSessionLocations(sid)
    //     viewModelScope.launch {
    //         Log.d("tanmay", "openSession: ${locations.first().size}")
    //         locations.first().forEach {
    //             _uiState.value = _uiState.value.copy(
    //                 routePoints = _uiState.value.routePoints + LatLng(it.lat, it.lng)
    //             )
    //         }
    //     }
    // }

    fun openSessionBySessionId(sid: Long) {
        viewModelScope.launch {
            val locations = repo.getSessionLocations(sid).first()
            Log.d("tanmay", "openSession: ${locations.first()}")
            val latLngs = locations.map { it ->
                LatLng(it.lat, it.lng)
            }
            _uiState.value = _uiState.value.copy(
                routePoints = latLngs
            )
        }
    }

    fun clearRouteBySessionId(sId: Long) {
        val locations = repo.getSessionLocations(sId)
        viewModelScope.launch {
            Log.d("tanmay", "openSession: ${locations.first().size}")
            locations.first().forEach {
                _uiState.value = _uiState.value.copy(
                    routePoints = _uiState.value.routePoints - LatLng(it.lat, it.lng)
                )
            }
        }
    }

    fun sessionStopped() {
        _uiState.value = _uiState.value.copy(
            sessionStartMs = 0
        )
        setSessionId(null)
        lastLatLngForPolyline = null
        _isServiceStarted.value = false
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {

            repo.deleteSession(sessionId)
        }
    }

}
