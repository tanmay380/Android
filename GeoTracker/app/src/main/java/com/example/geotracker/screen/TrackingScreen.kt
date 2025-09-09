package com.example.geotracker.screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.R
import com.example.geotracker.model.RoutePoint
import com.example.geotracker.ui.theme.primaryLightHighContrast
import com.example.geotracker.utils.Utils
import com.example.geotracker.utils.Utils.bitmapDescriptorFromVector
import com.example.geotracker.utils.Utils.formatDuration
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


var followMode: MutableState<Boolean> = mutableStateOf(true)

@OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onMyLocationClick: () -> Unit,
    stopService: () -> Unit,
    startOrStopService: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val state = rememberBottomSheetScaffoldState()

    val selectedPointRoute by viewModel.selectedPoint.collectAsState()

//    Log.d("tanmay", "TrackingScreen: ${uiStatePermission}")
    var myLocationClicked = false

    var context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    // Keep camera state stable across recompositions
    val cameraState = rememberCameraPositionState()


    // So the first fix snaps instantly, later fixes animate smoothly
    var firstFix by rememberSaveable { mutableStateOf(true) }

    // Optional: preserve zoom level the user left off with
    var userZoom by rememberSaveable { mutableFloatStateOf(17f) }

    var isMapLoaded by remember { mutableStateOf(false) }

    val isServiceStarted by viewModel.isServiceStarted.collectAsState()

    val density = LocalDensity.current

    var sheetHeightPx by remember { mutableStateOf(0f) }

    // While in follow mode, keep camera centered on each incoming location
    LaunchedEffect(followMode.value, isMapLoaded, myLocationClicked) {
        Log.d("tanmay", "TrackingScreen: $followMode  $myLocationClicked")
        if (!followMode.value || !isMapLoaded) return@LaunchedEffect
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
    val scope = rememberCoroutineScope()
    var hideJob by remember {
        mutableStateOf<Job?>(null)
    }
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

    var selectedId by remember { mutableStateOf<Set<Long?>>(emptySet()) }

//    val sessionStartMs = uiState.sessionStartMs

    val lastLocation by rememberSaveable {
        mutableStateOf<LatLng>(uiState.currentLatLng)
    }

    // get screen size to compute padding dynamically
    val config = LocalConfiguration.current
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

    var sheetOffset by remember { mutableStateOf(0f) }

    // Collect the dynamic offset safely
    LaunchedEffect(state.bottomSheetState) {
        snapshotFlow {
            // requireOffset() can throw before layout; guard it
            kotlin.runCatching { state.bottomSheetState.requireOffset() }.getOrNull() ?: 0f
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

    // runtime measured values
    var containerHeightPx by remember { mutableStateOf(0f) }
    var sheetTopY by remember { mutableStateOf(Float.POSITIVE_INFINITY) }
    var fabHeightPx by remember { mutableStateOf(0f) }

    // visual params
    val bottomPaddingDp = 12.dp
    val bottomPaddingPx = with(density) { bottomPaddingDp.toPx() }
    val overlapFactor = 0.5f // how much the FAB overlaps the sheet (0..1)

    val route = viewModel.routePoints.collectAsState().value


    AppWithDrawer(viewModel) { drawerState, set ->
        selectedId = set
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
                BootomScaffoldSheetContent(viewModel, uiState, state)

            }) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
//                    .padding(padding)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraState,
                    onMapLoaded = { isMapLoaded = true },
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                    ),
                    onMapClick = {latLng ->
                        // find nearest index
//                        val idx = findNearestRoutePointIndex(route, latLng.latitude, latLng.longitude)
//                        if (idx >= 0) {
//                            val rp = route[idx]
//                            val distMeters = haversineMeters(latLng.latitude, latLng.longitude, rp.lat, rp.lng)
//                            Log.d(TAG, "TrackingScreen: $rp ")// you can ignore dist or show it
//                            Log.d(TAG, "TrackingScreen: $distMeters" )// you can ignore dist or show it
//                        }\
                        viewModel.onMapTapped(latLng.latitude, latLng.longitude)

                        Log.d(TAG, "TrackingScreen: on map clocked")
                        resetControlsTimer(scope)
                    }

                ) {
                    MapEffect(Unit) { googleMap ->
                        googleMapRef = googleMap
                    }
                    MapEffect(followMode) { googleMap: GoogleMap ->
                        googleMap.setOnCameraMoveStartedListener { reason ->
                            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                                followMode.value = false
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
                    if (uiState.routePoints.isNotEmpty()) {
                        Polyline(
                            points = uiState.routePoints,
                            startCap = RoundCap(),
                            width = 13f
                        )
                    }

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
                                    selectedPointRoute!!.lat,
                                    selectedPointRoute!!.lng
                                )
                            ),
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
                    SelectedPointCard(selectedPointRoute!!, context)
                }
            }
        }

        val pair = MyLocationServiceStartComposable(
            controlsVisible,
            sheetOffset,
            screenHeightPx,
            onMyLocationClick,
            myLocationClicked,
            isServiceStarted,
            startOrStopService,
            viewModel,
            stopService,
            coroutineScope,
            context
        )
        myLocationClicked = pair.second


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
    context: Context
): Pair<Boolean, Boolean> {

    var myLocationClicked1 = myLocationClicked
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),

            ) {
//                    val offset = state.bottomSheetState.requireOffset()
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset({
                        IntOffset(
                            x = 0,
                            y = (sheetOffset - screenHeightPx + 70).toInt()
                        )
                    }),
                verticalArrangement = Arrangement.spacedBy(12.dp), // gap between buttons
                horizontalAlignment = Alignment.End
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        Log.d("tanmay", "TrackingScreen: button lodekd")
                        onMyLocationClick()
                        followMode.value = true
                        myLocationClicked1 = true
                    },

                    icon = {
                        Icon(
                            Icons.Filled.MyLocation,
                            contentDescription = "My Location"
                        )
                    },
                    text = { Text(if (followMode.value) "Following" else "My Location") }
                )

                FloatingActionButton(
                    onClick = {
                        if (!isServiceStarted) {
                            startOrStopService()
                            viewModel.setServiceStarted()
                        } else {
                            stopService()
                        }
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
        }
    }
    return Pair(followMode.value, myLocationClicked1)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BootomScaffoldSheetContent(
    viewModel: TrackingViewModel,
    uiState: TrackingUiState,
    drawerState: BottomSheetScaffoldState
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
                        viewModel.isServiceStarted.collectAsState().value.let { if (it) "Running" else "Stopped" },
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
                    StatColumn("${statelliteInfo.value?.usedInFix}/${statelliteInfo.value?.total}", "SATELLITE", weight = 0.8f, drawerState)
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
                    StatColumn( uiState.accuracy.toString() + " m", "ACCURACY", weight = 0.7f, drawerState)
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


@Composable
private fun ElapsedTimerText(sessionStartMs: Long): String {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
//    Log.d(TAG, "TrackingScreen session vlue: ${sessionStartMs}")


    // Tick every second
    LaunchedEffect(sessionStartMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    if (sessionStartMs > 0) {
        val elapsed = now - sessionStartMs
        val text =
            if (elapsed > 0) Utils.formatDuration(elapsed) else "Time: 00:00:00"
        return text
    }
    return "--"
}

@Composable
fun SelectedPointCard(selected: RoutePoint?, context: Context) {
    selected ?: return
    Card(modifier = Modifier.padding(12.dp)) {
        Column(Modifier.padding(8.dp)) {
            Text("Lat: ${"%.6f".format(selected.lat)}")
            Text("Lng: ${"%.6f".format(selected.lng)}")
            Text("Speed: ${"%.1f".format(selected.speed) } kmph")
            Text("time : ${Utils.formatEpoch(selected.timestamp)} ")
            Text("distance: ${selected.distanceClicked}" )
            // add timestamp etc.
        }
    }
}
