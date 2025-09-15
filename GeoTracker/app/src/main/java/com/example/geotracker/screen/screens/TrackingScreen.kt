package com.example.geotracker.screen.screens

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.translationMatrix
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.R
import com.example.geotracker.components.ElapsedTimerText
import com.example.geotracker.components.SelectedPointCard
import com.example.geotracker.location.service.LocationService
import com.example.geotracker.model.RoutePoint
import com.example.geotracker.model.TrackingUiState
import com.example.geotracker.screen.viewmodel.SharedViewModel
import com.example.geotracker.screen.viewmodel.TrackingViewModel
import com.example.geotracker.ui.theme.primaryLightHighContrast
import com.example.geotracker.utils.Utils.bitmapDescriptorFromVector
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.Projection
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import metersPerPixel
import minPixelDistanceToPolyline


@RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
@OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val state = rememberBottomSheetScaffoldState()

    val routePoints by viewModel.routePoints.collectAsState()

    val selectedPointRoute by viewModel.selectedPoint.collectAsState()

    var followMode by remember {
        mutableStateOf(true)
    }

    var myLocationClicked by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    // Keep camera state stable across recompositions
    val cameraState = rememberCameraPositionState()


    // So the first fix snaps instantly, later fixes animate smoothly
    var firstFix by rememberSaveable { mutableStateOf(true) }

    // Optional: preserve zoom level the user left off with
    var userZoom by rememberSaveable { mutableFloatStateOf(17f) }

    var isMapLoaded by remember { mutableStateOf(false) }

    val isServiceStarted by
    LocationService.isServiceRunning.collectAsStateWithLifecycle(false)

    val onMyLocationClick = { viewModel.startGettingLocation() }
    val stopService = {
        viewModel.sessionStopped()

    }
    val startOrStopService = {
        viewModel.startAndBindService()
    }

    val density = LocalDensity.current

    val scope = rememberCoroutineScope()
    var hideJob by remember {
        mutableStateOf<Job?>(null)
    }
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

    var selectedId by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // get screen size to compute padding dynamically
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    LaunchedEffect(true) {
        if (isServiceStarted) {
            Log.d(TAG, "TrackingScreen: ${uiState.sessionStartMs}")
            viewModel.onSelectionToggles(viewModel.sessionId.value!!)
        }
    }

    LaunchedEffect(selectedId) {
        Log.d(TAG, "TrackingScreen: launched effedct seelcrd id $isServiceStarted   ${selectedId.size}")
        if (isServiceStarted && selectedId.size < 2) return@LaunchedEffect
        followMode = false
    }

    // While in follow mode, keep camera centered on each incoming location
    LaunchedEffect(followMode, isMapLoaded, myLocationClicked) {
        Log.d(TAG, "TrackingScreen: $followMode value in launched effect")


        if (!followMode || !isMapLoaded) return@LaunchedEffect

        if (myLocationClicked) myLocationClicked = false

        snapshotFlow { uiState.currentLatLng }
            .filter { it.latitude != 0.0 || it.longitude != 0.0 } // ignore no-fix
            .distinctUntilChanged { a, b ->
                // treat tiny jitter as "same" (~1–2 meters)
                val dLat = a.latitude - b.latitude
                val dLng = a.longitude - b.longitude
                (dLat * dLat + dLng * dLng) < 1e-10
            }
            .collectLatest { ll ->
                if (!followMode) return@collectLatest
                if ((!isServiceStarted && selectedId.isNotEmpty() && !followMode)) return@collectLatest

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

    LaunchedEffect(isMapLoaded, selectedId) {

        Log.d(TAG, "TrackingScreen: $selectedId")

        val points = routePoints

//        Log.d(TAG, "TrackingScreen: $points")
        val maps = googleMapRef

        if (!isMapLoaded || maps == null)
        {
            Log.d(TAG, "TrackingScreen: not loaded")
            return@LaunchedEffect
        }
        if (points.isEmpty() || selectedId.isEmpty()) {
            // nothing to do
            Log.d(TAG, "TrackingScreen: nothing to do")
            return@LaunchedEffect

        }
        // for points
        if (points.size == 1) {
            val single = points.first()
            maps.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(single.lat, single.lng), 16f
                )
            )
            return@LaunchedEffect
        }

        val builder = LatLngBounds.builder()
        points.forEach {
            builder.include(LatLng(it.lat, it.lng))


        }
        val bounds = builder.build()


        val paddingPx = (minOf(screenWidthPx, screenHeightPx) * 0.12f).toInt()
        Log.d(TAG, "TrackingScreen: $bounds")
        maps.stopAnimation()
        maps.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))

    }

    var sheetOffset by remember { mutableStateOf(0f) }

    // Collect the dynamic offset safely
    LaunchedEffect(state.bottomSheetState) {
        snapshotFlow {
            // requireOffset() can throw before layout; guard it
            runCatching { state.bottomSheetState.requireOffset() }.getOrNull() ?: 0f
        }.collect { offset ->
            sheetOffset = offset
        }
    }
    var controlsVisible by remember { mutableStateOf(true) }

    fun resetControlsTimer(scope: CoroutineScope) {
        hideJob?.cancel()
        controlsVisible = true
        hideJob = scope.launch {
            delay(5000) // 5 seconds of inactivity
            controlsVisible = false
            Log.d(TAG, "resetControlsTimer: user is not touching")
        }
    }

    // visual params
    val bottomPaddingDp = 12.dp


    fun handleMapClick(
        clickLatLng: LatLng,
        polylines: List<RoutePoint>,     // your List<List<LatLng>>
        projection: Projection,
        zoom: Float,
        viewModel: TrackingViewModel            // or whichever VM
    ) {
        var listIndex = -1
        var bestIndex = -1
        var bestPx = Float.MAX_VALUE

        val pair = minPixelDistanceToPolyline(clickLatLng, polylines, projection)
        if (pair.first < bestPx) {
            bestPx = pair.first
            bestIndex = pair.second
        }


        if (bestIndex == -1) return

        val mpp = metersPerPixel(clickLatLng.latitude, zoom.toDouble())
        val distanceMeters = bestPx * mpp

        Log.d(
            TAG,
            "handleMapClick: clicked lat long ${clickLatLng.latitude}  ${clickLatLng.longitude}  \n" +
                    "${routePoints.get(bestIndex)}"
        )
        Log.d(TAG, "onMapTapped: $bestIndex   $distanceMeters")

        // send result to ViewModel (only primitives)
        viewModel.onMapTapped(bestIndex, distanceMeters)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppWithDrawer(viewModel, sharedViewModel) { drawerState, set ->
            selectedId = set as Set<Long>
            BottomSheetScaffold(
                sheetPeekHeight = 40.dp,
                scaffoldState = state,
                sheetContainerColor = Color.Black.copy(alpha = 0.4f),
                sheetContentColor = Color.Transparent,
                contentColor = Color.Transparent,
                containerColor = Color.Transparent,
                sheetDragHandle = {},
                sheetShape = RoundedCornerShape(0.dp),

                topBar = {
                    AnimatedVisibility(controlsVisible) {
                        TopAppBar(
                            title = {
                                Row() {
                                    Button(
                                        onClick = {
                                            navController.currentBackStackEntry?.savedStateHandle?.set("selectedId", selectedId)
                                            navController.navigate("Details Screen")

// Debug dump (run after navigate or in the destination):
                                            navController.currentBackStack.value.forEach { entry ->
                                                Log.d("tanmay", "BACKQUEUE: route=${entry.destination.route}  id=${entry.id}  keys=${entry.savedStateHandle.keys()}")
                                            }
                                            Log.d("tanmay", "current route=${navController.currentBackStackEntry?.destination?.route}")
                                            Log.d("tanmay", "previous route=${navController.previousBackStackEntry?.destination?.route}")
                                            Log.d("tanmay", "current keys=${navController.currentBackStackEntry?.savedStateHandle?.keys()}")
                                            Log.d("tanmay", "previous keys=${navController.previousBackStackEntry?.savedStateHandle?.keys()}")
                                            Log.d(TAG, "TrackingScreen: ${selectedId.size}")
    //                                            navController.navigate(DetailsScreenSelectedInfo(selectedId))
                                        },
                                        modifier = Modifier.padding(8.dp),
                                        shape = RoundedCornerShape(100.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent
                                        ),
                                        border = BorderStroke(
                                            2.dp,
                                            Color.White.copy(alpha = 0.6f)
                                        )
                                    ) {
                                        Text("Statistics", color = Color.White)
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()

                                    }
                                }) {
                                    Image(
                                        imageVector = Icons.Outlined.Menu,
                                        contentDescription = "Menu"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = primaryLightHighContrast
                            ),
                            actions = {

                            }
                        )


                    }

                },
                sheetContent = {
                    BootomScaffoldSheetContent(viewModel, uiState, state, isServiceStarted)

                }) { padding ->
                Box(
                    Modifier
                        .fillMaxSize()
//                    .padding(padding)
                ) {
                    var lat by remember { mutableStateOf<Double?>(null) }
                    var lng by remember { mutableStateOf<Double?>(null) }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState,
                        onMapLoaded = {
                            isMapLoaded = true
                            followMode = true
                        },
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                        ),
                        onMapClick = { latLng ->
                            viewModel.clearSelectedPoint()
                            resetControlsTimer(scope)
                        },
                        onMapLongClick = { latLng ->
//                            Log.d(TAG, "TrackingScreen: $routePoints")
                            handleMapClick(
                                LatLng(latLng.latitude, latLng.longitude),
                                routePoints,
                                googleMapRef!!.projection,
                                googleMapRef?.cameraPosition?.zoom!!,
                                viewModel
                            )

                            lat = latLng.latitude
                            lng = latLng.longitude

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

                        //for start point
                        if (uiState.routePoints.isNotEmpty() && selectedId.isNotEmpty()) {
                            Marker(
                                state = MarkerState(position = uiState.routePoints.first()),
                                title = "Start",
                                icon = bitmapDescriptorFromVector(
                                    context,
                                    R.drawable.outline_add_location_24
                                )
                            )
                        } //1757858959243
                        //for end point
                        //for start point
                        if (!isServiceStarted && uiState.routePoints.isNotEmpty() && selectedId.isNotEmpty()) {
//                            Log.d(TAG, "TrackingScreen: nside isservice started check")
                            Marker(
                                state = MarkerState(position = uiState.routePoints.last()),
                                title = "END",
                                icon = bitmapDescriptorFromVector(
                                    context,
                                    R.drawable.baseline_stop_circle_24
                                )
                            )
                        }

                        if (uiState.routePoints.isNotEmpty() && selectedId.isNotEmpty()) {
//                            Log.d(TAG, "TrackingScreen: is updating route points too")
                            Polyline(
                                points = uiState.routePoints,
                                startCap = RoundCap(),
                                width = 13f,
                                color = if (isServiceStarted) Color.Green else
                                    Color.Yellow
                            )
                        }

//                        Log.d(TAG, "TrackingScreen: ${uiState.displayPolylines}")

                        uiState.displayPolylines.forEach {
                            if (it.isEmpty()) return@forEach
                            Polyline(
                                points = it,
                                startCap = RoundCap(),
                                width = 13f,
                                color = Color.Yellow
                            )
                            Marker(
                                state = MarkerState(position = it.first()),
                                title = "Start",
                                icon = bitmapDescriptorFromVector(
                                    context,
                                    R.drawable.outline_add_location_24
                                )
                            )
                            Marker(
                                state = MarkerState(position = it.last()),
                                title = "Start",
                                icon = bitmapDescriptorFromVector(
                                    context,
                                    R.drawable.baseline_stop_circle_24
                                )
                            )

                        }
//                    }

                        Marker(
                            state = MarkerState(position = uiState.currentLatLng),
                            icon = bitmapDescriptorFromVector(
                                context,
                                R.drawable.img, // my current location maker
                                20, 20
                            ),
                            rotation = uiState.bearing
                        )
                        if (selectedPointRoute != null) {
                            Marker(
                                state = MarkerState(
                                    position = LatLng(
                                        lat!!,
                                        lng!!
                                    )
                                ),
                                onClick = { true },
                                title = "Selected",
                                icon = bitmapDescriptorFromVector(
                                    context,
                                    R.drawable.outline_add_location_24
                                )
                            )
                            // SelectedPointCard MOVED FROM HERE
                        }
                    } // GoogleMap ends

                    // SelectedPointCard MOVED TO HERE, wrapped in its conditional
                    if (selectedPointRoute != null) {
                        Log.d(
                            TAG,
                            "TrackingScreen: on map clocked $selectedPointRoute"
                        )


                        SelectedPointCard(selectedPointRoute!!, context)
                    }
                }
            }
            val onEFABClick = remember(onMyLocationClick) {
                {// Key on onMyLocationClick if it's a dependency
                    onMyLocationClick()      // Call your existing general map centering logic
                    followMode = true        // Update followMode state in TrackingScreen
                    myLocationClicked = true
                    Unit
                }// Update myLocationClicked state in TrackingScreen (if still used this way)
            }

            MyLocationServiceStartComposable(
                controlsVisible,
                sheetOffset,
                screenHeightPx,
                onEFABClick,
                myLocationClicked,
                isServiceStarted,
                startOrStopService = {
                    startOrStopService()
                    viewModel.setServiceStarted()
                    followMode = true
                },
                viewModel,
                stopService = { // Modified line
                    stopService() // Calls the original stopService from TrackingScreen's parameters
                    followMode = false // Sets the local followMode state
                },
                coroutineScope,
                followMode1 = followMode,
                context
            )
        }
    }
}


@Composable
private fun MyLocationServiceStartComposable(
    controlsVisible: Boolean,
    sheetOffset: Float,
    screenHeightPx: Float,
    onMyLocationClick: () -> Unit,
    myLocationClicked: Boolean,
    isServiceStarted: Boolean,
    startOrStopService: () -> Unit,
    viewModel: TrackingViewModel,
    stopService: () -> Unit,
    coroutineScope: CoroutineScope,
    followMode1: Boolean,
    context: Context
) {
    // keep a *stable* copy of the callback and followMode so child composables
    // can be remembered without changing identity each recomposition
    val onMyLocationClickState by rememberUpdatedState(onMyLocationClick)


    // compute translationY once per change (no heavy layout)
    val translationY by remember(sheetOffset, screenHeightPx) {
        mutableStateOf(sheetOffset - screenHeightPx + 70f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            // Put the column in a small stable composable to minimize the area that recomposes.
            FloatingControlsColumn(
                translationY = translationY,
                onMyLocationClick = { onMyLocationClickState() },
                isServiceStarted = isServiceStarted,
                startOrStopService = startOrStopService,
                viewModel = viewModel,
                stopService = stopService,
                coroutineScope = coroutineScope,
                context = context,
                followMode = followMode1
            )
        }
    }
}

@Composable
private fun FloatingControlsColumn(
    translationY: Float,
    onMyLocationClick: () -> Unit,
    isServiceStarted: Boolean,
    startOrStopService: () -> Unit,
    viewModel: TrackingViewModel,
    stopService: () -> Unit,
    coroutineScope: CoroutineScope,
    context: Context,
    followMode: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            // use graphicsLayer to translate (GPU) rather than Modifier.offset() which forces layout
            .graphicsLayer { translationMatrix(ty = translationY) }
    ) {
        ExtendedFloatingActionButton(
            onClick = onMyLocationClick,
            icon = {
                Icon(Icons.Filled.MyLocation, contentDescription = "My Location")
            },
            text = { Text(if (followMode) "Following" else "My Location") }
        )

        FloatingActionButton(
            onClick = {
                if (!isServiceStarted) {
                    startOrStopService()
                } else {
                    stopService()
                }
                // logging & toast are in click handler — OK here because it's not every compose
                coroutineScope.launch {
                    Toast.makeText(context, "dfdfdf", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            val image = if (!isServiceStarted) Icons.Filled.PlayArrow else Icons.Filled.Stop
            Image(imageVector = image, contentDescription = "Start/Stop")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BootomScaffoldSheetContent(
    viewModel: TrackingViewModel,
    uiState: TrackingUiState,
    drawerState: BottomSheetScaffoldState,
    isServiceStarted: Boolean
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val statelliteInfo = viewModel.satelliteState.collectAsState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null) {
                scope.launch {
                    Log.d(
                        TAG,
                        "TrackingScreen: clicked ${drawerState.bottomSheetState.currentValue}"
                    )
                    if (drawerState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded)
                        drawerState.bottomSheetState.expand()
                    else
                        drawerState.bottomSheetState.partialExpand()
                }
            }


    ) {
        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 12.dp)
                    .padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProvideTextStyle(
                    value = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = Color.White
                    )
                ) {
                    StatColumn(
                        "%.1f km/h".format(uiState.speed),
                        "SPEED",
                        weight = 1f,
                        drawerState
                    )
                    StatColumn(
                        if (isServiceStarted) "Running" else "Stopped",
                        "STATUS",
                        weight = 1f,
                        drawerState
                    )
                    StatColumn(
                        ElapsedTimerText(uiState.sessionStartMs),
                        "DURATION",
                        weight = 1.3f, drawerState
                    )
                    StatColumn(
                        "%.2f km".format(uiState.distance),
                        "DISTANCE",
                        weight = 1.1f,
                        drawerState
                    )
                    StatColumn("00", "BEARING", weight = 0.9f, drawerState)

                }
            }

            HorizontalDivider(Modifier.padding(top = 7.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProvideTextStyle(
                    value = LocalTextStyle.current.copy(
                        fontSize = 13.sp,
                        color = Color.White
                    )
                ) {
                    StatColumn(
                        "${statelliteInfo.value?.usedInFix}/${statelliteInfo.value?.total}",
                        "SATELLITE",
                        weight = 0.8f,
                        drawerState
                    )
                    Spacer(Modifier.width(8.dp))
                    StatColumn(uiState.elevation, "ELEVATION", weight = 1f, drawerState)
                    Spacer(Modifier.width(8.dp))
                    // Coordinates are long, give them more room and single-line ellipsis
                    StatColumnInline(
                        headline = "${uiState.currentLatLng.latitude}/${uiState.currentLatLng.latitude}",
                        subtext = "COORDINATES",
                        weight = 2.0
                    )
                    Spacer(Modifier.width(8.dp))
                    StatColumn(
                        uiState.accuracy.toString() + " m",
                        "ACCURACY",
                        weight = 0.7f,
                        drawerState
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun RowScope.StatColumn(
    headline: String,
    caption: String,
    weight: Float = 1f,
    drawerState: BottomSheetScaffoldState,
    maxHeadlineLines: Int = 1,
) {
    Column(
        modifier = Modifier
            .weight(weight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = headline,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = maxHeadlineLines,
            overflow = TextOverflow.Ellipsis
        )
//        if (drawerState.bottomSheetState.currentValue == SheetValue.Expanded)
        Text(
            text = caption,
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun RowScope.StatColumnInline(headline: String, subtext: String, weight: Double = 1.0) {
    Column(
        modifier = Modifier
            .weight(weight.toFloat())
            .padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = headline,
            style = LocalTextStyle.current.copy(fontSize = 12.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtext,
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(fontSize = 8.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

