package com.example.geotracker.helper

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// LocationProvider.kt
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val fused = LocationServices.getFusedLocationProviderClient(appContext)

    val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 1000L
    ).build()

    private var callback: LocationCallback? = null
    private val _locFlow = MutableSharedFlow<Location>(replay = 1)
    val locFlow = _locFlow.asSharedFlow()

    fun startUpdates() {
        Log.d("tanmay", "startUpdates: $callback")
        if (callback != null) return
        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                // emit off main thread if heavy, but SharedFlow works from here
                CoroutineScope(Dispatchers.Default).launch {
//                    Log.d("tanmay", "onLocationResult: $loc")
                    _locFlow.emit(loc) }
            }
        }
        try {
            fused.requestLocationUpdates(request, callback!!, Looper.getMainLooper())
        } catch (e: SecurityException) {
            // caller must ensure permissions
        }
    }

    fun stopUpdates() {
        callback?.let { fused.removeLocationUpdates(it) }
        callback = null
    }
}
