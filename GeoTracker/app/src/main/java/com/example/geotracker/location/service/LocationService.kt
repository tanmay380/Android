package com.example.geotracker.location.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.example.geotracker.MainActivity
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.R
import com.example.geotracker.model.LocationEntity
import com.example.geotracker.repository.LocationRepository
import com.example.geotracker.utils.Constants
import com.example.geotracker.utils.Constants.PREFS
import com.example.geotracker.utils.Constants.PREF_ACTIVE_SESSION
import com.example.geotracker.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {


    @Inject
    lateinit var repo: LocationRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // New Flow for LocationEntity
    private val _locationEntityFlow = MutableSharedFlow<Location>()
    val locationEntityFlow: SharedFlow<Location> = _locationEntityFlow


    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var currentSessionId: Long? = null // To store sessionId from intent

    // Binder given to clients
    private val binder = LocalBinder() // Added Binder


    // Inner class for the Binder
    inner class LocalBinder : Binder() {

        fun getService(): LocationService {
            Log.d("tanmay", "   getService: ")
            return this@LocationService
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("tanmay", "onCreate: service created")
        _isServiceRunning.value = true
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // It might be better to set _isServiceRunning to true in startLocationUpdates
        // or when actual work begins, depending on the definition of "running".
        // For now, onCreate covers the service's existence.
        Log.d("tanmay", "onStartCommand: ")
        return START_STICKY // Or START_NOT_STICKY / START_REDELIVER_INTENT depending on needs
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun fetchCurrentLocation(): Location? {
        val cts = CancellationTokenSource()
        Log.d("tanmay", "fetchCurrentLocation: ")
        return try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .await()
//            fusedLocationClient.requestLocationUpdates(
//                Prop,
//                locationCallback,
//                Looper.getMainLooper()
//            ).await()
        } catch (_: Exception) {
            null
        }
    }

    fun startLocationUpdates(currenid: Long) {
        if (currentSessionId == 0L) {
            Log.e("LocationService", "Session ID not set. Cannot start location updates.")
            return
        }
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L
        ).build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("LocationService", "Location permissions not granted.")
            // TODO: Consider how to handle this case. Maybe stopSelf()?
            return
        }
        startForeground(1, createNotification("Initializing..."))
        currentSessionId = currenid
        applicationContext.getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
            .edit {
                putLong(Constants.PREF_ACTIVE_SESSION, currentSessionId!!)
            }
        val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val sidFromPrefs = prefs.getLong(PREF_ACTIVE_SESSION, -1L)

        Log.d(TAG, "startLocationUpdates: $sidFromPrefs")

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
        _isServiceRunning.value = true // Explicitly set when updates start
        Log.d("tanmay", "Location updates started.")
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return

//            Log.d("tanmay", "onLocationResult: sending data")

//            Log.d("tanmay", "handleNewLocation: latitiude ${loc.latitude} long${loc.longitude}")


            val entity = LocationEntity(
                sessionId = currentSessionId!!, // Use the stored sessionId
                lat = loc.latitude,
                lng = loc.longitude,
                speed = loc.speed * 3.6f,
                timestamp = loc.time
            )

            // create a RoutePoint for map drawing
            println("service is ruunign")

            serviceScope.launch { // Launch coroutine to emit to the flow
//                Log.d("tanmay", "onLocationResult: $repo")
                repo?.insert(entity)
                if (loc.accuracy< 16)
                    _locationEntityFlow.emit(loc)
            }

            updateNotification(
                "Speed: %.1f km/h      ${Utils.formatDuration(System.currentTimeMillis() - currentSessionId!!)}".format(
                    loc.speed * 3.6
                )
            )
        }
    }

    private fun createNotification(text: String): Notification {
//        Log.d("tanmay", "createNotification: $currentSessionId")
        val intent =
            Intent(this, MainActivity::class.java).apply { // Assuming MainActivity is your main UI
                 Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = Notification.Builder(this, "geo_tracker")
            .setContentTitle("Geo Tracker")
            .setContentText(text)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this drawable exists
            .setContentIntent(pendingIntent)
        return builder.build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, createNotification(text))
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel("geo_tracker", "Geo Tracker", NotificationManager.IMPORTANCE_LOW)
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    // Return the binder for IPC
    override fun onBind(intent: Intent?): IBinder? {
        Log.d("tanmay", "Service bound")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("tanmay", "Service unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d("tanmay", "Service destroyed and location updates stopped.")
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceJob.cancel() // Cancel coroutines when service is destroyed
        applicationContext
            .getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
            .edit {
                remove(Constants.PREF_ACTIVE_SESSION)
            }
        _isServiceRunning.value = false // Service is no longer running
        stopSelf()
    }

    companion object {
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: SharedFlow<Boolean> = _isServiceRunning.asStateFlow()
    }
}