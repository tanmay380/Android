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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.translationMatrix
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.R
import com.example.geotracker.location.service.LocationService
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
    val state = rememberBottomSheetScaffoldState()

    val selectedPointRoute by viewModel.selectedPoint.collectAsState()

    var followMode by remember {
        mutableStateOf(true)
    }

    var myLocationClicked = false

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


    val density = LocalDensity.current

    var sheetHeightPx by remember { mutableStateOf(0f) }

    val scope = rememberCoroutineScope()
    var hideJob by remember {
        mutableStateOf<Job?>(null)
    }
    var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

    var selectedId by remember { mutableStateOf<Set<Long?>>(emptySet()) }

    var isFirstClickOnSelectedid by remember(selectedId) {
        mutableStateOf(true)
    }
    val lastLocation by rememberSaveable {
        mutableStateOf<LatLng>(uiState.currentLatLng)
    }

    // get screen size to compute padding dynamically
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    LaunchedEffect(true) {
        Log.d(TAG, "TrackingScreen: is sercide started")
        if (isServiceStarted){
            Log.d(TAG, "TrackingScreen: ${uiState.sessionStartMs}")
            viewModel.toggleSessionSelection(viewModel.sessionId.value!!)
        }
    }

    // While in follow mode, keep camera centered on each incoming location
    LaunchedEffect(followMode, isMapLoaded, myLocationClicked) {
        Log.d("tanmay", "TrackingScreen: followmode and location clicked $followMode  $myLocationClicked")
        if (!followMode || !isMapLoaded) return@LaunchedEffect

        snapshotFlow { uiState.currentLatLng }
            .filter { it.latitude != 0.0 || it.longitude != 0.0 } // ignore no-fix
            .distinctUntilChanged { a, b ->
                // treat tiny jitter as "same" (~1–2 meters)
                val dLat = a.latitude - b.latitude
                val dLng = a.longitude - b.longitude
                (dLat * dLat + dLng * dLng) < 1e-10
            }
            .collectLatest { ll ->
//                Log.d(TAG, "TrackingScreen: snapshot flow")
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
        val points = uiState.routePoints
        val maps = googleMapRef

//        Log.d(TAG, "TrackingScreen: first clicm $isFirstClickOnSelectedid")
        
        if (!isMapLoaded || maps == null) return@LaunchedEffect

        if (points.isEmpty() || selectedId.isEmpty())  {
            // nothing to do
            return@LaunchedEffect
        }

        if (points.size == 1) {
            val single = points.first()
            maps.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(single.latitude, single.longitude), 16f
                )
            )
            return@LaunchedEffect
        }

        val builder = LatLngBounds.builder()
        points.forEach {
//            Log.d(TAG, "TrackingScreen: ${it.longitude}, ${it.longitude}")
            builder.include(LatLng(it.latitude, it.longitude)) }
        val bounds = builder.build()
        

        val paddingPx = (minOf(screenWidthPx, screenHeightPx) * 0.12f).toInt()
//        Log.d(TAG, "TrackingScreen: bpunds $maps  \n $bounds"   )

        maps.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
//        maps.stopAnimation()
//        isFirstClickOnSelectedid = false

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
                    onMapLoaded = { isMapLoaded = true },
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                    ),
                    onMapClick = { latLng ->
                        resetControlsTimer(scope)
                    },
                    onMapLongClick = { latLng ->
                        viewModel.onMapTapped(latLng.latitude, latLng.longitude)
                        lat = latLng.latitude
                        lng = latLng.longitude


                        Log.d(
                            TAG,
                            "TrackingScreen: on map clocked ${selectedPointRoute?.distanceClicked}"
                        )


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
//                    Log.d(TAG, "TrackingScreen: ui sate riues ${uiState.routePoints?.size}")
                    if (uiState.routePoints.isNotEmpty() && selectedId.isNotEmpty()) {
                        Marker(
                            state = MarkerState(position = uiState.routePoints.first()),
                            title = "Start",
                            icon = bitmapDescriptorFromVector(
                                context,
                                R.drawable.outline_add_location_24
                            )
                        )
                    }
//                    Log.d(TAG, "TrackingScreen: $selectedId")
                    if (uiState.routePoints.isNotEmpty() && selectedId.isNotEmpty()) {
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
                        "TrackingScreen: on map clocked ${selectedPointRoute?.distanceClicked}"
                    )


                    SelectedPointCard(selectedPointRoute!!, context)
                }
            }
        }
        val onEFABClick = remember(onMyLocationClick) {
            {// Key on onMyLocationClick if it's a dependency
                onMyLocationClick()      // Call your existing general map centering logic
                followMode = true        // Update followMode state in TrackingScreen
                Log.d("TrackingScreen", "EFAB Clicked: followMode=true, myLocationClicked=true")
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
            startOrStopService,
            viewModel,
            stopService,
            coroutineScope,
            followMode1 = followMode,
            context
        )


    }
}

/*@Composable
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
): Unit{

    val followMode = remember {
        followMode1
    }


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
                    onClick = onMyLocationClick,
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
//                    Log.d(TAG, "MyLocationServiceStartComposable: $isServiceStarted")
                }
            }
        }
    }
}*/

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
    val followModeState by rememberUpdatedState(followMode1)

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
                followMode = followModeState
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
                    viewModel.setServiceStarted()
                } else {
                    stopService()
                }
                // logging & toast are in click handler — OK here because it's not every compose
                coroutineScope.launch {
                    Toast.makeText(context, "$isServiceStarted", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            val image = if (!isServiceStarted) Icons.Filled.PlayArrow else Icons.Filled.Stop
            Image(imageVector = image, contentDescription = "Start/Stop")
            // removed Log.d from the composition body
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
            if (elapsed > 0) formatDuration(elapsed) else "Time: 00:00:00"
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
            Text("Speed: ${"%.1f".format(selected.speed)} kmph")
            Text("time : ${Utils.formatEpoch(selected.timestamp)} ")
            Text("distance: ${selected.distanceClicked}")
            // add timestamp etc.
        }
    }
}
