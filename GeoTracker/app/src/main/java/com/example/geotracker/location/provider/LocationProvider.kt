package com.example.geotracker.location.provider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.geotracker.model.SatelliteInfo
import com.example.geotracker.screen.TrackingViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// LocationProvider.kt
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val fused = LocationServices.getFusedLocationProviderClient(appContext)

    val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 2000L
    ).setWaitForAccurateLocation(true)
        .build()


    private var callback: LocationCallback? = null
    private val _locFlow = MutableSharedFlow<Location>(replay = 1)
    val locFlow = _locFlow.asSharedFlow()

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startUpdates() {
        Log.d("tanmay", "Lcoation Provider startUpdates: $callback")
        if (callback != null) return
        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                // emit off main thread if heavy, but SharedFlow works from here
                CoroutineScope(Dispatchers.Default).launch {
//                    Log.d("tanmay", "onLocationResult: $loc")
                    _locFlow.emit(loc)
                }
            }
        }
        try {
            fused.requestLocationUpdates(request, callback!!, Looper.getMainLooper())
        } catch (e: SecurityException) {
            // caller must ensure permissions
        }
    }

    fun satelliteFlow(): Flow<SatelliteInfo> = callbackFlow {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            trySend(SatelliteInfo(0,0, System.currentTimeMillis()))
            close()
            return@callbackFlow
        }

        val cb = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val total = status.satelliteCount
                var used = 0
                for (i in 0 until total) if (status.usedInFix(i)) used++
                trySend(SatelliteInfo(total, used, System.currentTimeMillis())).isSuccess
            }
        }

        val executor = ContextCompat.getMainExecutor(appContext)
        locationManager.registerGnssStatusCallback(cb, null)

        awaitClose { locationManager.unregisterGnssStatusCallback(cb) }
    }.conflate()


    fun stopUpdates() {
        callback?.let { fused.removeLocationUpdates(it) }
        callback = null
    }
}