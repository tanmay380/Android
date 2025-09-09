package com.example.geotracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.geotracker.screen.TrackingScreen
import com.example.geotracker.screen.TrackingViewModel
import com.example.geotracker.location.service.LocationService
import com.example.geotracker.ui.theme.GeoTrackerTheme
import com.example.geotracker.utils.Constants.PREFS
import com.example.geotracker.utils.Constants.PREF_ACTIVE_SESSION
import com.example.permissions.PermissionGateSequential
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// MainActivity.kt (refactor)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        val TAG = "tanmay"
    }

    private val viewModel: TrackingViewModel by viewModels()
    private var boundService: LocationService? = null
    private var isBound = false
    private var updatesJob: Job? = null


    // Queue one-shot work that should run as soon as we're bound
    private var pendingAction: ((LocationService) -> Unit)? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: $viewModel?")
            Log.d(TAG, "onServiceConnected")
            val binder = service as LocationService.LocalBinder
            boundService = binder.getService()
            isBound = true

            // Start (or restart) the continuous collector for live UI updates
            updatesJob?.cancel()
            updatesJob = lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    Log.d(TAG, "onServiceConnected: $viewModel")
                    boundService!!.locationEntityFlow.collect { entity ->
                        viewModel.handleNewLocation(entity)
                    }
                }
            }

            // Run any queued one-shot action (e.g., fetch current location)
            pendingAction?.invoke(boundService!!)
            pendingAction = null
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            boundService = null
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext)
        handleIntentForSession(intent)

        setContent {
            GeoTrackerTheme {
//                ensurePermissionsIfYouNeedTo()
                PermissionGateSequential {

                    TrackingScreen(

                        viewModel = viewModel,
                        // 👉 START/RESUME continuous tracking
                        startOrStopService = {
                            startService()
                            viewModel.createSessionIfAbsent()

                            pendingOrNow { svc -> svc.startLocationUpdates(viewModel.sessionId.value!!) }
                        },
                        stopService = {
                            Log.d(TAG, "onCreate: stop service called")
                            if (isBound) {
                                boundService = null
                                isBound = false
                            }
                            viewModel.sessionStopped()
                            unbindService(connection)
                            stopService(Intent(this, LocationService::class.java))
                        },
                        // 👉 ONE-SHOT: get current location on demand
                        onMyLocationClick = {
//                        startService()
                            viewModel.startGettingLocation()

                            // ensure service exists & bound (no-op if already)
                        }
                    )
                }

//                startAndBindService()
            }
        }

        // If you still run a permission gate, call it here; do NOT auto-start/bind in onStart.
    }

    private fun startService() {
        val intent = Intent(this, LocationService::class.java)
        // Start in STARTED mode first so it survives when Activity unbinds/minimizes
        ContextCompat.startForegroundService(this, intent)
        // Then bind for UI streaming
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    /** Run [action] immediately if bound, otherwise queue it and bind now. */
    private fun pendingOrNow(action: (LocationService) -> Unit) {
        val svc = boundService
        if (svc != null) {
            action(svc)
        } else {
            pendingAction = action
        }
    }

    private fun startAndBindService() {
        Log.d(TAG, "startAndBindService: ")
        val intent = Intent(this, LocationService::class.java)
        // Start in STARTED mode first so it survives when Activity unbinds/minimizes
        ContextCompat.startForegroundService(this, intent)
        // Then bind for UI streaming
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // --- Lifecycle cleanup ---
    override fun onDestroy() {
        super.onDestroy()
        updatesJob?.cancel()
        if (isBound) {
            unbindService(connection)
            isBound = false
            boundService = null
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isBound) {
//            startAndBindService()
        }
    }

    private fun handleIntentForSession(intent: Intent?) {
        val sidFromIntent = intent?.getLongExtra("EXTRA_SESSION_ID", -1L) ?: -1L
        val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val sidFromPrefs = prefs.getLong(PREF_ACTIVE_SESSION, -1L)

        val restoredSessionId = when {
            sidFromIntent > 0L -> sidFromIntent
            sidFromPrefs > 0L -> sidFromPrefs
            else -> -1L
        }

        Log.d(TAG, "handleIntentForSession: $restoredSessionId")

        if (restoredSessionId > 0L) {
            viewModel.setSessionId(restoredSessionId)
            viewModel.openSessionBySessionId(restoredSessionId)
            // ensure we bind so the viewModel starts receiving live updates from service
            startAndBindService() // optionally start/bind if you want to reattach
        } else {
            // no active session — viewModel stays without session until user starts one
        }

    }

    // If you want reopen-from-notification to rebind (without auto-starting a new service):
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d(TAG, "onNewIntent: $isBound")
        viewModel.isFromIntent.value = true
        if (!isBound) {
            // Only bind; do NOT call startForegroundService here
            bindService(
                Intent(this, LocationService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
        handleIntentForSession(intent)
    }

    // ---- Permissions (keep whatever you had; just don't auto-start service here) ----
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun ensurePermissionsIfYouNeedTo() {
        val permissions =
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )


        val multiplePermissionsState = rememberMultiplePermissionsState(permissions)

        Column {
            when {
                multiplePermissionsState.allPermissionsGranted -> {
                    Text("All permissions granted 🎉")
                }

                multiplePermissionsState.shouldShowRationale -> {
                    Column {
                        Text("We need these permissions for location & notifications.")
                        Button(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
                            Text("Allow")
                        }
                    }
                }

                else -> {

                    SideEffect {
                        multiplePermissionsState.launchMultiplePermissionRequest()
                    }
                }
            }
        }
    }

}
