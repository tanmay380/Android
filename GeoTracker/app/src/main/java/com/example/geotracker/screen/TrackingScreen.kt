package com.example.geotracker.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.R
import com.example.geotracker.ui.theme.primaryLightHighContrast
import com.example.geotracker.utils.Utils
import com.example.geotracker.utils.Utils.bitmapDescriptorFromVector
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
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
import kotlinx.coroutines.launch

@OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onMyLocationClick: () -> Unit,
    stopService: () -> Unit,
    startOrStopService: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiStatePermission by viewModel.uiState.collectAsState()

//    Log.d("tanmay", "TrackingScreen: ${uiStatePermission}")
    var myLocationClicked = false

    var context = LocalContext.current

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

    val isServiceStarted by viewModel.isServiceStarted.collectAsState()


    // While in follow mode, keep camera centered on each incoming location
    LaunchedEffect(followMode, isMapLoaded, myLocationClicked) {
        Log.d("tanmay", "TrackingScreen: $followMode  $myLocationClicked")
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

    var selectedId by remember { mutableStateOf<Set<Long?>>(emptySet()) }


    // get screen size to compute padding dynamically
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    LaunchedEffect(isMapLoaded, uiState.routePoints) {
        val points = uiState.routePoints
        val maps = googleMapRef

        if (!isMapLoaded || maps == null) return@LaunchedEffect

        if (points.isEmpty() || selectedId.isEmpty()) {
            // nothing to do
            return@LaunchedEffect
        }

        if (points.size == 1) {
            val single = points.first()
            maps.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    com.google.android.gms.maps.model.LatLng(single.latitude, single.longitude), 16f
                )
            )
            return@LaunchedEffect
        }

        val builder = LatLngBounds.builder()
        points.forEach { builder.include(LatLng(it.latitude, it.longitude)) }
        val bounds = builder.build()

        val paddingPx = (minOf(screenWidthPx, screenHeightPx) * 0.12f).toInt()

        maps.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))


    }

    AppWithDrawer(viewModel) { drawerState, set ->
        selectedId = set
        Scaffold(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars), topBar = {
            TopAppBar(
                title = {
                    Row() {

                        Text("this is a text")
                        Text("this is another text")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.open()

                        }
                    }) {
                        Image(imageVector = Icons.Outlined.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryLightHighContrast
                ),
                actions = {

                }
            )
        }) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraState,
                    onMapLoaded = { isMapLoaded = true },
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                    ),
                    onMapClick = {
                        Log.d(TAG, "TrackingScreen: on map clocked")
                    }

                ) {
                    MapEffect(Unit) { googleMap ->
                        googleMapRef = googleMap
                    }
                    MapEffect(followMode) { googleMap: GoogleMap ->
                        googleMap.setOnCameraMoveStartedListener { reason ->
                            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                                followMode = false
// Remember the zoom the user prefers while exploring
                                userZoom = cameraState.position.zoom
                            }
                        }
                    }
                    if (uiState.routePoints.isNotEmpty()) {
                        Marker(
                            state = MarkerState(position = uiState.routePoints.first()),
                            title = "Start",
                            icon = bitmapDescriptorFromVector(
                                context,
                                R.drawable.outline_add_location_24
                            )
                        )

                    }

                    Polyline(
                        points = uiState.routePoints,
                        startCap = RoundCap()
                    )
                    Marker(state = MarkerState(position = uiState.currentLatLng))
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Log.d(TAG, "TrackingScreen session vlue: ${uiState.sessionStartMs}")
                    ElapsedTimerText(uiState.sessionStartMs)
                    Text("Speed: %.1f km/h".format(uiState.speed), color = Color.Black)
                    Text("Distance: %.2f km".format(uiState.distance), color = Color.Black)
                    Text("Avg: %.1f km/h".format(uiState.avgSpeed), color = Color.Black)

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
                            followMode = true
                            myLocationClicked = true
                        },

                        icon = {
                            Icon(
                                Icons.Filled.MyLocation,
                                contentDescription = "My Location"
                            )
                        },
                        text = { Text(if (followMode) "Following" else "My Location") }
                    )

                    FloatingActionButton(
                        onClick = {
                            if (!isServiceStarted)
                                startOrStopService()
                            else {
                                stopService()
                                viewModel.setSessionId(null)

                            }
                            viewModel.setServiceStarted(!isServiceStarted)
                            coroutineScope.launch {
                                Toast.makeText(
                                    context,
                                    "$isServiceStarted",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        },
                    ) {
                        Image(
                            imageVector = if (!isServiceStarted) Icons.Filled.PlayArrow else
                                Icons.Filled.Stop, contentDescription = "Start/Stop"
                        )
                    }
                }
//        } else {
//            Text("Location permission denied. Please grant permission to use this feature.")
//        }
            }
        }
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
    if (sessionStartMs > 0) {
        val elapsed = now - sessionStartMs
        val text = if (elapsed > 0) "Time: ${Utils.formatDuration(elapsed)}" else "Time: 00:00:00"
        Text(text, color = Color.Black)
    }
}
