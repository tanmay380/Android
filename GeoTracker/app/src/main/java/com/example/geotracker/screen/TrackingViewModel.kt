package com.example.geotracker.screen


import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.components.SessionSummary
import com.example.geotracker.location.provider.LocationProvider
import com.example.geotracker.location.service.LocationService
import com.example.geotracker.model.LocationEntity
import com.example.geotracker.model.RoutePoint
import com.example.geotracker.model.SatelliteInfo
import com.example.geotracker.model.TrackingUiState
import com.example.geotracker.repository.LocationRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val app: Application, // Hilt injects Application
    private val repo: LocationRepository,
    private val locationProvider: LocationProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shared = locationProvider.satelliteFlow()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    val satelliteState: StateFlow<SatelliteInfo?> =
        shared.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val MIN_POLYLINE_DISTANCE_M = 2.0// inside ViewModel class
    private var lastLatLngForPolyline: LatLng? = null

    var isFromIntent = mutableStateOf(false)

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState

    val sessionSummary: StateFlow<List<SessionSummary>> =
        repo.getAllSessionSummariesFlow().map {
            it
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var totalDistance = 0.0
    private var lastLatLng: LatLng? = null

    private var _sessionId = MutableStateFlow<Long?>(savedStateHandle["sessionId"])
    var sessionId: StateFlow<Long?> = _sessionId.asStateFlow()

    fun setSessionId(id: Long?) {
        _sessionId.value = id
    }

    private val _routePoints = MutableStateFlow<List<RoutePoint>>(emptyList())
    val routePoints: StateFlow<List<RoutePoint>> = _routePoints

    private val TAG = "tanmay"

    // Service references
    private var boundService: LocationService? = null
    private var isBound = false
    private var updatesJob: Job? = null

    // Optional: queue a one-shot action to run once bound
    private var pendingAction: ((LocationService) -> Unit)? = null

    // Exposed UI state: latest location
    private val _latestLocation = MutableStateFlow<LocationEntity?>(null)
    val latestLocation: StateFlow<LocationEntity?> = _latestLocation.asStateFlow()

    // Expose whether service is bound/running (for UI)
    private val _isBound = MutableStateFlow(false)
    val isBoundFlow = _isBound.asStateFlow()


    // backing mutable + public read-only selection flow
    private val _selectedSessionId = MutableStateFlow<Set<Long?>>(emptySet())
    val selectedSessionId: StateFlow<Set<Long?>> = _selectedSessionId.asStateFlow()

    private val _selectedPoint = MutableStateFlow<RoutePoint?>(null)
    val selectedPoint: StateFlow<RoutePoint?> = _selectedPoint



    // ServiceConnection lives here
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            val local = binder as LocationService.LocalBinder
            boundService = local.getService()
            isBound = true
            _isBound.value = true

            // cancel any previous collector
            updatesJob?.cancel()
            updatesJob = viewModelScope.launch {
                // simple continuous collect; you can debounce or transform if needed
                boundService!!.locationEntityFlow.collect { entity ->
                    handleNewLocation(entity)
                }
            }

            // run queued action (if any)
            pendingAction?.invoke(boundService!!)
            pendingAction = null
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")
            isBound = false
            boundService = null
            _isBound.value = false
            updatesJob?.cancel()
        }
    }

    // Start and bind: uses application context → survives activity unbinds
    fun startAndBindService() {
        val intent = Intent(app, LocationService::class.java)
        ContextCompat.startForegroundService(app, intent)
        // bind with app context — will keep connection independent of Activity lifecycle
        app.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // Just bind (no startForegroundService) — useful if service already running
    fun bindServiceOnly() {
        if (isBound) return
        val intent = Intent(app, LocationService::class.java)
        app.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // Run action now if bound, otherwise queue it and ensure binding
    fun pendingOrNow(action: (LocationService) -> Unit) {
        boundService?.let { action(it) } ?: run {
            pendingAction = action
            bindServiceOnly()
        }
    }

    // Unbind / cleanup
    fun unbindService() {
        if (isBound) {
            try {
                app.unbindService(connection)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "unbindService failed: ${e.message}")
            }
            isBound = false
            boundService = null
            _isBound.value = false
        }
        updatesJob?.cancel()
        updatesJob = null
    }

    // Optional: stop the service (if you want)
    fun stopService() {
        unbindService()
        val intent = Intent(app, LocationService::class.java)
        app.stopService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        // Very important — ensure we unbind to not leak the connection when VM dies
        unbindService()
    }

    fun addRoutePoint(point: RoutePoint) {
        _routePoints.update { it + point }
    }
    fun addRoutePointList(list: List<RoutePoint>){
//        Log.d(TAG, "addRoutePointList: ${_routePoints.value}    $list")
        _routePoints.update { it + list }
    }

    fun clearRoute() {
        _routePoints.value = emptyList()
    }

    fun setServiceStarted() {
        _uiState.update {
            it.copy(
                sessionStartMs = System.currentTimeMillis()
            )
        }

    }

    fun openSession(sId: Long) {
        viewModelScope.launch {
//            openSessionBySessionId(sId)
        }

    }

    init {
        startGettingLocation()
        viewModelScope.launch {
            uiState
                .collectLatest { state ->

//                Log.d("tanmay", "combined points: staste value $state")
                    val combined = listOf(state.routePoints)
                        .filter { it.isNotEmpty() } /*+
                            state.displayPolylines.filter { it.isNotEmpty() }*/

                    val flatRoutePoints: List<RoutePoint> = combined
                        .flatten()
                        .map { latLng ->
                            latLng.toRoutePoint(state.speed.toFloat(), System.currentTimeMillis())
                        }

                    addRoutePointList(flatRoutePoints)
                }
        }
    }

    private suspend fun updateDisplayedPolylines(current1: Set<Long?>) {
        val polylines = mutableListOf<List<LatLng>>()
        current1.forEach { sessionId ->
            // Assuming getSessionLocations returns Flow<List<LocationEntity>>
            // and LocationEntity has lat, lng
            val locations =
                repo.getSessionLocations(sessionId!!).first() // Collect the first emission
            if (sessionId != _sessionId.value)
                polylines.add(locations.map { LatLng(it.lat, it.lng) })

            val detailedRoutePoint = locations.map {
                RoutePoint(
                    lat = it.lat,
                    lng = it.lng,
                    speed = it.speed * 3.6f,
                    timestamp = it.timestamp
                )
            }
            addRoutePointList(detailedRoutePoint)
        }
//        if (polylines.isEmpty()) return
        _uiState.update { it.copy(displayPolylines = polylines) }
    }


    fun onMapTapped(bestIndex: Int, distance: Double): Double {
//        val response = findNearestRoutePointIndex(_routePoints.value, lat, lng)

        Log.d(TAG, "onMapTapped: $bestIndex   $distance")

//        if (response.first < 0) return -1.0

//        if (isClickNearPolyline(LatLng(lat, lng), combinedPoints))

//        if (response.second < 10) {
//            Log.d("tanmay", "onMapTapped: viewmodel vlaue ${response.second}")
        _selectedPoint.value = routePoints.value.get(bestIndex)

        _selectedPoint.value =
            _selectedPoint.value!!.copy(distanceClicked = distance.toFloat())
        /*}
        else{
            _selectedPoint.value = RoutePoint(
                lat = lat,
                lng = lng,
                speed = 0f,
                timestamp = System.currentTimeMillis(),
                distanceClicked = response.second.toFloat()
            )
        }*/
        return 0.0
    }
    fun clearSelectedPoint() { _selectedPoint.value = null }

    /** Toggle selection: select id if not selected, otherwise clear selection. */
    fun toggleSessionSelection(sessionId: Long) {
        val newSelection = _selectedSessionId.value.toMutableSet().apply {
            if (contains(sessionId)) remove(sessionId) else add(sessionId)
        }.toSet()

        viewModelScope.launch {
            updateDisplayedPolylines(newSelection)   // use the future selection
            _selectedSessionId.value = newSelection  // update state only after polylines updated
        }
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

        locationProvider.stopUpdates()
        clearSelection()
        Log.d("tanmay", "createSessionIfAbsent: $sessionId")
        toggleSessionSelection(_sessionId.value!!)
        _uiState.value = TrackingUiState()

        Log.d("tanmay", "createSessionIfAbsent: ${sessionId.value}")
    }
    //1757617840252

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startGettingLocation() {
        locationProvider.startUpdates()
        viewModelScope.launch {
            locationProvider.locFlow.collect {
//                Log.d("tanmay", "startGettingLocation: ${it.bearing}")
                _uiState.value = _uiState.value.copy(
                    currentLatLng = LatLng(it.latitude, it.longitude),
                    bearing = it.bearing,
                    elevation = "${it.altitude.toInt()}±${it.mslAltitudeAccuracyMeters}",
                    accuracy = it.accuracy.toInt(),
                    speed = it.speed * 3.6
                )
            }
        }
    }

    fun handleNewLocation(entity: Location) {

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
            Location.distanceBetween(
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
        createRoutePoints(entity)

        val elapsedHours = (entity.time - (sessionId.value!!)) / 1000.0 / 3600.0
        _uiState.update { cur ->
            val totalKm = cur.distance
            val avg = if (elapsedHours > 0) totalKm / elapsedHours else cur.avgSpeed
            cur.copy(
                avgSpeed = avg,
                speed = entity.speed * 3.6,
                bearing = entity.bearing,
                elevation = "${entity.altitude.toInt()}±${entity.mslAltitudeAccuracyMeters.toInt()}"
            )
        }
    }


    fun openSessionBySessionId(sid: Long) {
        Log.d(TAG, "openSessionBySessionId: $sid")
        if (sid == _sessionId.value) return

        viewModelScope.launch {
            val locations = repo.getSessionLocations(sid).first()
            Log.d("tanmay", "openSession: ${locations.first()}")

            val detailedRoutePoint = locations.map {
                RoutePoint(
                    lat = it.lat,
                    lng = it.lng,
                    speed = it.speed,
                    timestamp = it.timestamp
                )
            }
//            _routePoints.value = detailedRoutePoint
            Log.d(TAG, "openSessionBySessionId: $locations    $detailedRoutePoint")
            addRoutePointList(detailedRoutePoint)
            if (!_selectedSessionId.value.contains(sid)) {
                toggleSessionSelection(sid) // This will call updateDisplayedPolylines
            } else {
                // If already selected, updateDisplayedPolylines might still be needed
                // if the data could have changed or to ensure consistency.
                // However, toggleSessionSelection handles the update.
            }
            isFromIntent.value = true
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
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {

            repo.deleteSession(sessionId)
        }
    }

}

private fun TrackingViewModel.createRoutePoints(entity: Location) {
    val point = RoutePoint(
        lat = entity.latitude,
        lng = entity.longitude,
        speed = entity.speed * 3.6f,
        timestamp = entity.time,
//        distanceClicked =
    )

    addRoutePoint(point)
}

private fun LatLng.toRoutePoint(defaultSpeed: Float = 0f, defaultTs: Long = 0L) =
    RoutePoint(lat = latitude, lng = longitude, speed = defaultSpeed, timestamp = defaultTs)

