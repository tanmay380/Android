package com.example.geotracker.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geotracker.utils.Utils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onMyLocationClick: () -> Unit,
    startOrStopService: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiStatePermission by viewModel.uiState.collectAsState()

//    Log.d("tanmay", "TrackingScreen: ${uiStatePermission}")
    var myLocationClicked = false

    val coroutineScope = rememberCoroutineScope()
    // Keep camera state stable across recompositions
    val cameraState = rememberCameraPositionState()

    // Follow mode: true → auto-center; false → user controls camera
    var followMode by rememberSaveable { mutableStateOf(true) }

    // So the first fix snaps instantly, later fixes animate smoothly
    var firstFix by rememberSaveable { mutableStateOf(true) }

    // Optional: preserve zoom level the user left off with
    var userZoom by rememberSaveable { mutableFloatStateOf(17f) }

    var isMapLoaded by remember { mutableStateOf(false) }

//    val locationPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions(),
//        onResult = {
//            val isGranted = it.values.all { it }
//            viewModel.onPermissionResult(isGranted)
//        }
//    )
//
//    LaunchedEffect(Unit) {
//        val permissions = arrayOf(
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )
//        if (permissions.any { ContextCompat.checkSelfPermission(ctx,
//                it) != PackageManager.PERMISSION_GRANTED }) {
//            locationPermissionLauncher.launch(permissions)
//        } else {
//            viewModel.onPermissionResult(true)
//        }
//    }


    // While in follow mode, keep camera centered on each incoming location
    LaunchedEffect(followMode, isMapLoaded, myLocationClicked) {
        if (!followMode || !isMapLoaded) return@LaunchedEffect
        Log.d("tanmay", "TrackingScreen: ")
        Log.d("Tanmay", "TrackingScreen: ${uiState.currentLatLng}")
        snapshotFlow { uiState.currentLatLng }
            .filter { it.latitude != 0.0 || it.longitude != 0.0 } // ignore no-fix
            .distinctUntilChanged { a, b ->
                // treat tiny jitter as "same" (~1–2 meters)
                val dLat = a.latitude - b.latitude
                val dLng = a.longitude - b.longitude
                (dLat * dLat + dLng * dLng) < 1e-10
            }
            .collectLatest { ll ->
                if (firstFix) {
                    cameraState.move(CameraUpdateFactory.newLatLngZoom(ll, userZoom))
                    firstFix = false
                } else {
                    cameraState.animate(
                        update = CameraUpdateFactory.newLatLng(ll),
                        durationMs = 350
                    )
                }
            }
    }

    Box(Modifier.fillMaxSize()) {
//        if (uiState.locationPermissionGranted) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            onMapLoaded = { isMapLoaded = true },
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
            )

        ) {
            MapEffect(followMode) { googleMap: GoogleMap ->
                googleMap.setOnCameraMoveStartedListener { reason ->
                    if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                        followMode = false
// Remember the zoom the user prefers while exploring
                        userZoom = cameraState.position.zoom
                    }
                }
            }


            Polyline(points = uiState.routePoints)
            Marker(state = MarkerState(position = uiState.currentLatLng))
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ElapsedTimerText(uiState.sessionStartMs)
            Text("Speed: %.1f km/h".format(uiState.speed))
            Text("Distance: %.2f km".format(uiState.distance))
            Text("Avg: %.1f km/h".format(uiState.avgSpeed))

        }


        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), // gap between buttons
            horizontalAlignment = Alignment.End
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    Log.d("tanmay", "TrackingScreen: button lodekd")
                    onMyLocationClick()
                    myLocationClicked = true
                },

                icon = { Icon(Icons.Filled.LocationOn, contentDescription = "My Location") },
                text = { Text(if (followMode) "Following" else "My Location") }
            )

            FloatingActionButton(
                onClick = { startOrStopService() },
            ) {
                Image(imageVector = Icons.Filled.PlayArrow, contentDescription = "Start/Stop")
            }
        }
//        } else {
//            Text("Location permission denied. Please grant permission to use this feature.")
//        }
    }
}

@Composable
private fun ElapsedTimerText(sessionStartMs: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Tick every second
    LaunchedEffect(sessionStartMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val elapsed = now - sessionStartMs
    Text("Time: ${Utils.formatDuration(elapsed)}")
}
