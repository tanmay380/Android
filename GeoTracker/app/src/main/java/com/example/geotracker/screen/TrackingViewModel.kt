package com.example.geotracker.screen

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.model.LocationEntity
import com.example.geotracker.repository.LocationRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TrackingUiState(
    val currentLatLng: LatLng = LatLng(0.0, 0.0),
    val routePoints: List<LatLng> = emptyList(),
    val distance: Double = 0.0,
    val speed: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val locationPermissionGranted: Boolean = false,
    val sessionStartMs: Long = 0L
)


class TrackingViewModel(
    private val repo: LocationRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState
    val uiStatePermission: Boolean = _uiState.value.locationPermissionGranted

    private var totalDistance = 0.0
    private var lastLatLng: LatLng? = null
    private val sessionId = System.currentTimeMillis()
    private val sessionStartMs: Long = System.currentTimeMillis()


    //    private val receiver = object : BroadcastReceiver() {
//        override fun onReceive(ctx: Context?, intent: Intent?) {
//            val lat = intent?.getDoubleExtra("lat", 0.0) ?: return
//            val lng = intent.getDoubleExtra("lng", 0.0)
//            val speed = intent.getFloatExtra("speed", 0f)
//            val time = intent.getLongExtra("time", System.currentTimeMillis())
//
//            val entity = LocationEntity(
//                sessionId = sessionId,
//                lat = lat,
//                lng = lng,
//                speed = speed,
//                timestamp = time
//            )
////            Log.d("tanmay", "onReceive: ")
//            addLocation(entity)
//        }
//    }
//
//    init {
//        context.registerReceiver(receiver, IntentFilter("LOCATION_UPDATE"),
//            Context.RECEIVER_EXPORTED)
//    }
    init {
        _uiState.value = _uiState.value.copy(sessionStartMs = sessionStartMs)
    }

    fun onPermissionResult(isGranted: Boolean) {
        Log.d("tanmay", "onPermissionResult: value is $isGranted")
        _uiState.value = _uiState.value.copy(locationPermissionGranted = isGranted)
    }

    fun getLocationAndMakePath(){
        repo.getSessionLocations(sessionStartMs)
    }

    fun handleNewLocation(entity: Location) {
        Log.d("tanmay", "handleNewLocation: ")
        val location = LocationEntity(
            sessionId = sessionId,
            lat = entity.latitude,
            lng = entity.longitude,
            speed = entity.speed,
            timestamp = entity.time
        )

        viewModelScope.launch {
            repo.insert(location)

            val newLL = LatLng(location.lat, location.lng)
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

            val elapsedHrs = (location.timestamp - sessionId) / 1000.0 / 3600.0
            _uiState.value = _uiState.value.copy(
                currentLatLng = newLL,
                routePoints = _uiState.value.routePoints + newLL,
                distance = totalDistance / 1000.0,
                speed = location.speed * 3.6,
                avgSpeed = if (elapsedHrs > 0) (totalDistance / 1000.0) / elapsedHrs else 0.0
            )

        }


    }

//
//    fun addLocation(entity: LocationEntity) {
//        viewModelScope.launch {
//            repo.insert(entity)
//
//            val newLatLng = LatLng(entity.lat, entity.lng)
//
//            lastLatLng?.let {
//                val results = FloatArray(1)
//                android.location.Location("").apply {
//                    latitude = it.latitude
//                    longitude = it.longitude
//                }.distanceTo(
//                    android.location.Location("").apply {
//                        latitude = newLatLng.latitude
//                        longitude = newLatLng.longitude
//                    }
//                ).also { d -> totalDistance += d }
//            }
//            lastLatLng = newLatLng
//
//            val elapsedHours = (entity.timestamp - sessionId) / 1000.0 / 3600.0
//
//            _uiState.value = _uiState.value.copy(
//                currentLatLng = newLatLng,
//                routePoints = _uiState.value.routePoints + newLatLng,
//                distance = totalDistance / 1000,
//                speed = entity.speed * 3.6,
//                avgSpeed = if (elapsedHours > 0) (totalDistance / 1000) / elapsedHours else 0.0
//            )
//        }
//    }


    override fun onCleared() {
        super.onCleared()
    }

    fun openSession(sid: Long) {

        val locations = repo.getSessionLocations(sid)
        Log.d("tanmay", "openSession: $locations")
        viewModelScope.launch {
            locations.collect {
                Log.d("tanmay", "openSession: $it")
                it.forEach {
                    _uiState.value = _uiState.value.copy(
                        routePoints = _uiState.value.routePoints + LatLng(it.lat, it.lng)
                    )
                }
            }
        }


    }

}
