package com.example.geotracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.example.geotracker.screen.viewmodel.TrackingViewModel
import com.example.geotracker.navigation.GeoTrackerNavigation
import com.example.geotracker.screen.viewmodel.SharedViewModel
import com.example.geotracker.ui.theme.GeoTrackerTheme
import com.example.geotracker.utils.Constants.PREFS
import com.example.geotracker.utils.Constants.PREF_ACTIVE_SESSION
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.AndroidEntryPoint

// MainActivity.kt (refactor)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        val TAG = "tanmay"
    }

    val sharedViewModel: SharedViewModel by viewModels()
    val viewModel: TrackingViewModel by viewModels()
   /* private var boundService: LocationService? = null
    private var isBound = false
    private var updatesJob: Job? = null

    private var isServiceRunning  = false


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
    }*/

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext)
        WindowCompat.setDecorFitsSystemWindows(window, false)
// Set nav bar color (use theme color)
        window.navigationBarColor = Color(0xFF121212).toArgb() // or transparent if root draws background
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false


        setContent {
            GeoTrackerTheme {
                GeoTrackerNavigation(
                    rememberNavController(),
                    "Main Screen Route",
                    viewModel,
                    sharedViewModel
                )
                /*PermissionGateSequential {

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
                            viewModel.startGettingLocation()

                        },
                        // 👉 ONE-SHOT: get current location on demand
                        onMyLocationClick = {
//                        startService()
                            viewModel.startGettingLocation()

                            // ensure service exists & bound (no-op if already)
                        }
                    )
                }*/

//                startAndBindService()
            }
        }

        // If you still run a permission gate, call it here; do NOT auto-start/bind in onStart.
    }

//    private fun startService() {
//        val intent = Intent(this, LocationService::class.java)
//        // Start in STARTED mode first so it survives when Activity unbinds/minimizes
//        ContextCompat.startForegroundService(this, intent)
//        // Then bind for UI streaming
//        bindService(intent, connection, BIND_AUTO_CREATE)
//    }
//
//    //** Run [action] immediately if bound, otherwise queue it and bind now. *//*
//    private fun pendingOrNow(action: (LocationService) -> Unit) {
//        val svc = boundService
//        if (svc != null) {
//            action(svc)
//        } else {
//            pendingAction = action
//        }
//    }
//
    private fun handleIntentForSession() {

        val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val sidFromPrefs = prefs.getLong(PREF_ACTIVE_SESSION, -1L)

        Log.d(TAG, "handleIntentForSession: $sidFromPrefs  " )
        val restoredSessionId = when {
            sidFromPrefs > 0L -> sidFromPrefs
            else -> -1L
        }

        Log.d(TAG, "handleIntentForSession: $restoredSessionId")

        if (restoredSessionId > 0L) {
            viewModel.bindServiceOnly()
            viewModel.openSessionBySessionId(restoredSessionId)
        } else {
            // no active session — viewModel stays without session until user starts one
        }

    }

    override fun onStop() {
        super.onStop()
        viewModel.unbindService()
    }

    // --- Lifecycle cleanup ---
    override fun onDestroy() {
        super.onDestroy()
//        viewModel.stopService()
    }

    override fun onResume() {
        super.onResume()
        handleIntentForSession()
    }

    // If you want reopen-from-notification to rebind (without auto-starting a new service):
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//
//        Log.d(TAG, "onNewIntent: $isBound")
//        viewModel.isFromIntent.value = true
//        if (!isBound) {
//            // Only bind; do NOT call startForegroundService here
//            bindService(
//                Intent(this, LocationService::class.java),
//                connection,
//                Context.BIND_AUTO_CREATE
//            )
//        }
//        handleIntentForSession(intent)
//    }

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
