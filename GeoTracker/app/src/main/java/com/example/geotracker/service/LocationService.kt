package com.example.geotracker.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.example.geotracker.MainActivity
import com.example.geotracker.R
import com.example.geotracker.model.LocationEntity
import com.example.geotracker.utils.Utils
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // private val _locationUpdates = MutableSharedFlow<LocationResult>() // Commented out old flow
    // val locationFlow: SharedFlow<LocationResult> = _locationUpdates // Commented out old flow

    // New Flow for LocationEntity
    private val _locationEntityFlow = MutableSharedFlow<Location>()
    val locationEntityFlow: SharedFlow<Location> = _locationEntityFlow


    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var currentSessionId: Long =
        System.currentTimeMillis() // To store sessionId from intent

    // Binder given to clients
    private val binder = LocalBinder() // Added Binder

    // Inner class for the Binder
    inner class LocalBinder : Binder() {

        fun getService(): LocationService {
            Log.d("tanmay", "getService: ")
             return this@LocationService
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("tanmay", "onCreate: service created")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        Log.d("Tanmay", "onCreate: service $currentSessionId")
        // Notification text will be updated once session ID is known
        startForeground(1, createNotification("Initializing..."))
        // startLocationUpdates() // Will be called after sessionId is set
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentSessionId = intent?.getLongExtra("SESSION_ID", System.currentTimeMillis())!!
        Log.d("LocationService", "Session ID received: $currentSessionId")
//        startLocationUpdates() // Start updates once session ID is available
//        updateNotification("Tracking active. Session: $currentSessionId")
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

    fun startLocationUpdates() {
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
        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.d("tanmay", "Location updates started.")
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
//            Log.d("tanmay", "onLocationResult: sending data")
            val loc = result.lastLocation ?: return

            val entity = LocationEntity(
                sessionId = currentSessionId, // Use the stored sessionId
                lat = loc.latitude,
                lng = loc.longitude,
                speed = loc.speed,
                timestamp = loc.time
            )

            serviceScope.launch { // Launch coroutine to emit to the flow
                _locationEntityFlow.emit(loc)
            }

            // Commented out Broadcast location
            // val intent = Intent("LOCATION_UPDATE")
            // intent.putExtra("lat", loc.latitude)
            // intent.putExtra("lng", loc.longitude)
            // intent.putExtra("speed", loc.speed)
            // intent.putExtra("time", loc.time)
            // Log.d("tanmay", "onLocationResult: ${loc.longitude} , ${loc.latitude}")
            // sendBroadcast(intent)

            updateNotification(
                "Speed: %.1f km/h      ${Utils.formatDuration(System.currentTimeMillis() - currentSessionId)}".format(
                    loc.speed * 3.6
                )
            )
        }
    }

    private fun createNotification(text: String): Notification {
        Log.d("tanmay", "createNotification: $currentSessionId")
        val intent =
            Intent(this, MainActivity::class.java).apply { // Assuming MainActivity is your main UI
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("isOpenedFromNoti", true)
                putExtra("EXTRA_SESSION_ID", currentSessionId)
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
        Log.d("tanmay", "createNotificationChannel: $currentSessionId")
        val channel =
            NotificationChannel("geo_tracker", "Geo Tracker", NotificationManager.IMPORTANCE_LOW)
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    // Return the binder for IPC
    override fun onBind(intent: Intent?): IBinder? {
        Log.d("LocationService", "Service bound")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("LocationService", "Service unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LocationService", "Service destroyed and location updates stopped.")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceJob.cancel() // Cancel coroutines when service is destroyed
    }
}
