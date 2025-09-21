package com.example.geotracker.screen.viewmodel


import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.location.provider.LocationProvider
import com.example.geotracker.location.service.LocationService
import com.example.geotracker.model.LocationEntity
import com.example.geotracker.model.RoutePoint
import com.example.geotracker.model.SatelliteInfo
import com.example.geotracker.model.TrackingUiState
import com.example.geotracker.repository.LocationRepository
import com.example.geotracker.repository.SelectionManager
import com.example.geotracker.utils.Constants.PREFS
import com.example.geotracker.utils.Constants.PREF_ACTIVE_SESSION
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val app: Application, // Hilt injects Application
    private val repo: LocationRepository,
    private val locationProvider: LocationProvider,
    private val savedStateHandle: SavedStateHandle,
    private var selectionManager: SelectionManager
) : ViewModel() {

    private val shared = locationProvider.satelliteFlow()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    val satelliteState: StateFlow<SatelliteInfo?> =
        shared.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val MIN_POLYLINE_DISTANCE_M = 2.0// inside ViewModel class
    private var lastLatLngForPolyline: LatLng? = null

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState

    private var totalDistance = 0.0
    private var lastLatLng: LatLng? = null

    val sessionId = selectionManager.runningSessionId


    private val selectedSessionId = selectionManager.selectedSessionId.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptySet()
    )

    fun setSessionId(id: Long?) {
        selectionManager.setRunningSession(id)
    }

    private val _routePoints = MutableStateFlow<List<RoutePoint>>(emptyList())
    val routePoints: StateFlow<List<RoutePoint>> = _routePoints

    private val TAG = "tanmay"

    private val _requestedIds = MutableStateFlow<Set<Long>>(emptySet())

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

    private val updateMutex = Mutex()


    private val _selectedPoint = MutableStateFlow<RoutePoint?>(null)
    val selectedPoint: StateFlow<RoutePoint?> = _selectedPoint


    // ServiceConnection lives here
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val local = binder as LocationService.LocalBinder
            boundService = local.getService()
            isBound = true
            _isBound.value = true

            // cancel any previous collector
            updatesJob?.cancel()
            updatesJob = viewModelScope.launch {
                Log.d(TAG, "updatesJob CREATED: $this")
                displayPolylineIndex = if (uiState.value.displayPolylines.size == 0) 0 else
                    uiState.value.displayPolylines.size - 1
                // simple continuous collect; you can debounce or transform if needed
                boundService!!.locationEntityFlow.collect { entity ->
                    ensureActive()
                    handleNewLocation(entity)
                }
                Log.d(TAG, "updatesJob collector finished")
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

            Log.d(TAG, "clearing updatesJob (was: $updatesJob, active=${updatesJob?.isActive})")
            updatesJob?.cancel()
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.d(TAG, "onBindingDied: ")
            super.onBindingDied(name)
        }
    }

    init {
        startGettingLocation()
        viewModelScope.launch {
            uiState
                .collectLatest { state ->
                    val combined = listOf(state.routePoints)
                        .filter { it.isNotEmpty() } /*+
                            state.displayPolylines.filter { it.isNotEmpty() }*/

                    val flatRoutePoints: List<RoutePoint> = combined
                        .flatten()
                        .map { latLng ->
                            latLng.toRoutePoint(state.speed.toFloat(), System.currentTimeMillis())
                        }
//                    _routePoints.value = flatRoutePoints
//                    addRoutePointList(flatRoutePoints)
                }
        }
        viewModelScope.launch {
            _requestedIds
                .collectLatest {
                    updateDisplayedPolylines(it)
                }
        }
    }


    // Start and bind: uses application context → survives activity unbinds
    fun startAndBindService() {
        val intent = Intent(app, LocationService::class.java)
        ContextCompat.startForegroundService(app, intent)
        // bind with app context — will keep connection independent of Activity lifecycle
        app.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        pendingOrNow { svc ->
            svc.startLocationUpdates(sessionId.value!!)
            selectionManager.toggleSessionSelection(sessionId.value!!)
        }

    }

    // Run action now if bound, otherwise queue it and ensure binding
    fun pendingOrNow(action: (LocationService) -> Unit) {
        boundService?.let { action(it) } ?: run {
            pendingAction = action
            bindServiceOnly()
        }
    }

    // Just bind (no startForegroundService) — useful if service already running
    fun bindServiceOnly() {
        if (isBound) return
        val intent = Intent(app, LocationService::class.java)
        app.bindService(intent, connection, Context.BIND_AUTO_CREATE)


        createSessionIfAbsent()
    }

    // Unbind / cleanup
    fun unbindService() {
        Log.d(
            TAG,
            "unbindService() called — ${this}  isBound=$isBound, updatesJob=$updatesJob, active=${updatesJob?.isActive}  ${routePoints.value.size}"
        )

        // cancel the collector (fire & forget)
//        updatesJob?.cancel()
//        updatesJob = null
        Log.d(TAG, "updatesJob cancelled and cleared. now updatesJob=$updatesJob")

        if (isBound) {
            try {
                app.unbindService(connection)
                Log.d(TAG, "unbindService: success")
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "unbindService failed: ${e.message}")
            } finally {
                isBound = false
                boundService = null
                _isBound.value = false
            }
        } else {
            Log.d(TAG, "unbindService: not bound, skipping unbind")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: $isBound   $updatesJob")
    }


    // Optional: stop the service (if you want)
    fun stopService() {
        unbindService()
        val intent = Intent(app, LocationService::class.java)
        app.stopService(intent)
    }

    fun logCaller(tag: String = "tanmay") {
        val stackTrace = Throwable().stackTrace
        Log.d(TAG, "logCaller: ${stackTrace.toList()}")
//        if (stackTrace.size >= 3) {
//            val caller = stackTrace[3] // [0] is Throwable, [1] is this function, [2] is the caller
//            android.util.Log.d(tag, "Called from: ${caller.className}.${caller.methodName} (${caller.fileName}:${caller.lineNumber})")
//        } else {
//            android.util.Log.d(tag, "Caller not found")
//        }
    }


    fun addRoutePoint(point: RoutePoint) {
        _routePoints.update { it + point }
    }

    fun addRoutePointList(list: List<RoutePoint>) {
        logCaller()
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

    fun openSessionBySessionId(sid: Long) {
        Log.d(TAG, "openSessionBySessionId: $sid")
//        if (sid == _sessionId.value) return

        viewModelScope.launch {
            val locations = repo.getSessionLocationsList(sid)

            val detailedRoutePoint = locations.map { it ->
                _uiState.update { ui ->
                    ui.copy(
                        routePoints = ui.routePoints + LatLng(it.lat, it.lng)
                    )
                }
                RoutePoint(
                    lat = it.lat,
                    lng = it.lng,
                    speed = it.speed,
                    timestamp = it.timestamp
                )
            }

            addRoutePointList(detailedRoutePoint)
            Log.d(TAG, "openSessionBySessionId: selected session id ${selectedSessionId.value}")
            if (!selectedSessionId.value.contains(sid)) {
                selectionManager.toggleSessionSelection(sid) // This will call updateDisplayedPolylines
            }
            Log.d(TAG, "openSessionBySessionId: selected session id ${selectedSessionId.value}")

        }
    } //1758446769794

    fun clearRouteBySessionId(sId: Long) {
        val locations = repo.getSessionLocations(sId)
        viewModelScope.launch {
            locations.first().forEach {
                _uiState.value = _uiState.value.copy(
                    routePoints = _uiState.value.routePoints - LatLng(it.lat, it.lng)
                )
            }
        }
    }


    suspend fun updateDisplayedPolylines(current1: Set<Long>) {
        updateMutex.withLock {
            Log.d(TAG, "updateDisplayedPolylines: $current1")
            clearRoute()
            val polylines = mutableListOf<List<LatLng>>()
            current1.forEach { id ->
                // Assuming getSessionLocations returns Flow<List<LocationEntity>>
                // and LocationEntity has lat, lng
                val locations =
                    repo.getSessionLocationsList(id) // Collect the first emission
                if (id != sessionId.value)
                    polylines.add(locations.map { LatLng(it.lat, it.lng) })

                val detailedRoutePoint = locations.map {
                    RoutePoint(
                        lat = it.lat,
                        lng = it.lng,
                        speed = it.speed * 3.6f,
                        timestamp = it.timestamp
                    )
                }
                Log.d(TAG, "updateDisplayedPolylines: route points will be updated now")
                addRoutePointList(detailedRoutePoint)
            }
            _uiState.update { it.copy(displayPolylines = polylines) }
//        if (polylines.isEmpty()) return
            Log.d(
                TAG, "updateDisplayedPolylines: ${uiState.value.displayPolylines.size}  \n " +
                        "route points ${uiState.value.routePoints.size} \n" +
                        "route points variable ${routePoints.value.size}"
            )

        }
    }

    /*
        fun onSelectionToggles(sessionId: Long) {
            selectionManager.toggleSessionSelection(sessionId) {
                Log.d(TAG, "onSelectionToggles: $it")
                updateDisplayedPolylines(it)
            }
        }
    */

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

    fun clearSelectedPoint() {
        _selectedPoint.value = null
    }


    /** Optional helper to explicitly select (useful when opening session programmatically). */
    fun createSessionIfAbsent() {
        val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val sidFromPrefs = prefs.getLong(PREF_ACTIVE_SESSION, -1L)
        var id: Long? = null

        if (sessionId.value == null || sidFromPrefs == -1L) {
            id = System.currentTimeMillis()
            sessionId.value = id
            _uiState.value = _uiState.value.copy(sessionStartMs = id)
        } else {
            id = sidFromPrefs
        }


        selectionManager.setRunningSession(id)
        locationProvider.stopUpdates()
        selectionManager.clearSelection()
        Log.d("tanmay", "createSessionIfAbsent: $sessionId")
        _uiState.value = TrackingUiState()
        clearRoute()
        Log.d("tanmay", "createSessionIfAbsent: ${sessionId.value}")
    }
//1757617840252

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startGettingLocation() {
        try {
            throw Exception("tanmay")
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                /*Log.d(
                    TAG, "start getting location: ${uiState.value.displayPolylines.size}  \n " +
                            "route points ${uiState.value.routePoints.size} \n" +
                            "route points variable ${routePoints.value.size}")*/
            }
        }
    }


    var displayPolylineIndex = 0

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
                elevation = "${entity.altitude.toInt()}±${entity.mslAltitudeAccuracyMeters.toInt()}",
            )
        }
    }

    fun sessionStopped() {
        /*val updated = if (displayPolylineIndex in _uiState.value.displayPolylines.indices) {
            cur.displayPolylines.mapIndexed { i, poly -> if (i == displayPolylineIndex) poly + newLL else poly }
        } else {
            // index missing -> append a new polyline containing newLL
            cur.displayPolylines + listOf(listOf(newLL))
        }*/
        _uiState.value = _uiState.value.copy(
            sessionStartMs = 0,
            displayPolylines = listOf(_uiState.value.routePoints) + uiState.value.displayPolylines,
            routePoints = emptyList(),
        )
        setSessionId(null)
        selectionManager.setRunningSession(null)
        lastLatLngForPolyline = null

        unbindService()
        stopService()
        startGettingLocation()

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

