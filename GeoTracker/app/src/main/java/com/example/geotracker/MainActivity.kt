//// MainActivity.kt
package com.example.geotracker

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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.room.Room
import com.example.geotracker.dao.AppDatabase
import com.example.geotracker.repository.LocationRepository
import com.example.geotracker.screen.TrackingScreen
import com.example.geotracker.screen.TrackingViewModel
import com.example.geotracker.service.LocationService
import com.example.geotracker.ui.theme.GeoTrackerTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//class MainActivity : ComponentActivity() {
//
//    public val TAG = "tanmay"
//    private lateinit var viewModel: TrackingViewModel
//    private var boundService: LocationService? = null
//    private var isBound = false
//    // MainActivity.kt — add a Job to manage the collector
//    private var updatesJob: Job? = null
//
//
//    // private var isOpenedFromNoti = false // Commented out as it's not used
//    private var serviceDeferred: CompletableDeferred<LocationService>? = null
//
//    private val connection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            Log.d(TAG, "onServiceConnected: ")
//            val binder = service as LocationService.LocalBinder
//            boundService = binder.getService()
//            isBound = true
//            updatesJob?.cancel()
//            updatesJob = lifecycleScope.launch {
//                repeatOnLifecycle(Lifecycle.State.STARTED) {
//                    boundService?.locationEntityFlow?.collect { loc ->
//                        viewModel.handleNewLocation(loc)  // ✅ updates UI state on every fix
//                    }
//                }
//            }
//
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            isBound = false
//            boundService = null
//        }
//    }
//
//    // Call this instead of directly using `boundService`
//    private suspend fun getBoundService(): LocationService {
//        boundService?.let { return it }
//        val intent = Intent(this, LocationService::class.java)
//        ContextCompat.startForegroundService(this, intent)
//        bindService(intent, connection, BIND_AUTO_CREATE)
//        serviceDeferred = CompletableDeferred()
//        return serviceDeferred!!.await()
//    }
//
//
//    /*
//        private val connection = object : ServiceConnection {
//            override fun onServiceConnected(
//                name: ComponentName?,
//                service: IBinder?
//            ) {
//                val binder = service as LocationService.LocalBinder
//                boundService = binder.getService()
//                isBound = true
//
//                lifecycleScope.launch {
//                    repeatOnLifecycle(Lifecycle.State.STARTED) {
//                        boundService!!.locationEntityFlow.collect {
//                            viewModel.handleNewLocation(it)
//    //                        Log.d(TAG, "onServiceConnected: $it")
//                        }
//                    }
//                }
//
//            }
//
//            override fun onServiceDisconnected(name: ComponentName?) {
//                isBound = false
//                boundService = null
//            }
//        }
//    */
//
//    private val baseLocationPerms = arrayOf(
//        Manifest.permission.ACCESS_FINE_LOCATION,
//        Manifest.permission.ACCESS_COARSE_LOCATION
//    )
//
//    private val notifPerm =
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
//            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
//        else emptyArray()
//
//    private val backgroundPerm =
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//        else emptyArray()
//
//    private val requestPerms =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
//            val allGranted = grants.values.all { it }
//            if (allGranted) {
//                maybeRequestBackgroundThenCheckBattery()
//            } else {
//                Log.w(TAG, "Location and/or Notification permissions not granted")
//                // You can show a dialog/snackbar here explaining why permission is needed.
//            }
//        }
//
//    private val requestBackground =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
//            if (granted) {
//                Log.d(TAG, "Background location permission granted.")
//            } else {
//                Log.w(TAG, "Background location permission not granted.")
//            }
//            // Whether granted or not, proceed to check battery optimizations
//            checkAndRequestBatteryOptimization()
//        }
//
//    private val requestIgnoreBatteryOptimizations =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            // User has returned from the battery optimization settings screen.
//            // We can now proceed to start the service, regardless of their choice,
//            // as the app should ideally function without this exemption too.
//            Log.d(TAG, "Returned from battery optimization settings.")
////            startAndBindService()
//        }
//
//    @SuppressLint("MissingPermission")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        Log.d(TAG, "onCreate: " + intent)
//
//        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "geo_tracker_db")
//            .build()
//        val repo = LocationRepository(db.locationDao())
//        viewModel = TrackingViewModel(
//            repo,
//            applicationContext
//        ) // Consider Application context for ViewModel if appropriate
//
//        setContent {
//            GeoTrackerTheme {
//                TrackingScreen(
//                    viewModel = viewModel,
//                    onMyLocationClick = {
//                        lifecycleScope.launch {
//                            val svc = getBoundService()
//                            svc.fetchCurrentLocation().let {
//                                viewModel.handleNewLocation(it!!)
//                            }
//                        }
//
//                    }) {
//                    lifecycleScope.launch {
//                        val svc = getBoundService()
//                        Log.d(TAG, "onCreate: $svc")
//                        svc.startLocationUpdates()
//                    }
//                }
//            }
//        }
//        ensurePermissionsAndStart()
//    }
//
//    private fun ensurePermissionsAndStart() {
////        Log.d(TAG, "ensurePermissionsAndStart")
//        val needsBase = baseLocationPerms.any { notGranted(it) }
//        val needsNotif = notifPerm.any { notGranted(it) }
//
//        when {
//            needsBase || needsNotif -> {
////                Log.d(TAG, "Requesting base location and/or notification permissions.")
//                requestPerms.launch(baseLocationPerms + notifPerm)
//            }
//
//            else -> {
////                Log.d(TAG, "Base permissions already granted.")
//                maybeRequestBackgroundThenCheckBattery()
//            }
//        }
//    }
//
//    private fun maybeRequestBackgroundThenCheckBattery() {
////        Log.d(TAG, "maybeRequestBackgroundThenCheckBattery")
//        if (backgroundPerm.isNotEmpty() && notGranted(backgroundPerm[0])) {
////            Log.d(TAG, "Requesting background location permission.")
//            requestBackground.launch(backgroundPerm[0])
//        } else {
////            Log.d(TAG, "Background location permission already granted or not required.")
//            checkAndRequestBatteryOptimization()
//        }
//    }
//
//    private fun checkAndRequestBatteryOptimization() {
////        Log.d(TAG, "checkAndRequestBatteryOptimization")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
//            val packageName = packageName
//            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
////                Log.d(TAG, "Requesting to ignore battery optimizations.")
//                // It's a good practice to show a dialog to the user explaining why
//                // this is needed before redirecting them to settings.
//                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
//                    data = Uri.parse("package:$packageName")
//                }
//                if (intent.resolveActivity(packageManager) != null) {
//                    requestIgnoreBatteryOptimizations.launch(intent)
//                } else {
//                    Log.w(
//                        TAG,
//                        "Device cannot handle ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS intent."
//                    )
////                    startAndBindService() // Proceed if the setting is unavailable
//                }
//            } else {
////                Log.d(TAG, "App is already ignoring battery optimizations.")
////                startAndBindService()
//            }
//        } else {
//            // Not needed for versions below Marshmallow
////            Log.d(TAG, "Battery optimization exemption not needed for this Android version.")
////            startAndBindService()
//        }
//    }
//    // MainActivity.kt — clean up
////    override fun onDestroy() {
////        super.onDestroy()
////        Log.d(TAG, "onDestroy: hello")
////        updatesJob?.cancel()
////        if (isBound) unbindService(connection)
////    }
////
//
//
//    private fun startAndBindService() {
//        Log.d(TAG, "startAndBindService called")
//        val serviceIntent = Intent(this, LocationService::class.java).apply {
//            // Pass session ID or any other necessary data to the service
//            // putExtra("SESSION_ID", viewModel.sessionId.value) // Example if ViewModel holds session ID
//        }
//        try {
//            ContextCompat.startForegroundService(this, serviceIntent)
//            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
//            viewModel.onPermissionResult(true) // Notify ViewModel that permissions are set
//            Log.d(TAG, "Service started and bound.")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error starting or binding service: ${e.message}", e)
//            // Handle exceptions, e.g., if the service cannot be started in the foreground (missing permissions, etc.)
//        }
//    }
//
//    private fun notGranted(p: String) =
//        ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED
//
////    override fun onStart() {
////        super.onStart()
////        // Re-bind to the service if it's already running and activity is restarting
////        // This ensures the connection is re-established if it was unbound in onStop
////        // Only bind if the permissions are already granted and service might be running.
////        // The main binding logic is now after permission checks and battery optimization.
////        // However, if the service is already running (e.g. app was backgrounded and reopened)
////        // and we are already bound, we might not need to call bindService again explicitly here
////        // if the original `isBound` logic handles it.
////        // For simplicity, let's ensure we attempt to bind if not already bound,
////        // assuming permissions are granted (which they should be if we reach onStart after initial setup)
////        if (!isBound && allPermissionsGranted()) { // allPermissionsGranted() would be a helper
////            Log.d(TAG, "onStart: Attempting to re-bind service.")
////            val intent = Intent(this, LocationService::class.java)
////            try {
////                // Check if service is actually running before trying to bind
////                bindService(intent, connection, Context.BIND_AUTO_CREATE)
////            } catch (e: Exception) {
////                Log.e(TAG, "Error rebinding service in onStart: ${e.message}")
////            }
////        }
////    }
//
//    private fun allPermissionsGranted(): Boolean {
//        val baseGranted = baseLocationPerms.all { !notGranted(it) }
//        val notifGranted = if (notifPerm.isNotEmpty()) notifPerm.all { !notGranted(it) } else true
//        val backgroundGranted =
//            if (backgroundPerm.isNotEmpty()) !notGranted(backgroundPerm[0]) else true
//        return baseGranted && notifGranted && backgroundGranted
//    }
//
//
////    override fun onStop() {
////        super.onStop()
////        if (isBound) {
////            try {
////                unbindService(connection)
////                Log.d(TAG, "Service unbound in onStop.")
////            } catch (e: IllegalArgumentException) {
////                Log.w(TAG, "Service already unbound or not properly bound: ${e.message}")
////            }
////            isBound = false
////            // boundService = null // Clearing instance here might be too early if onStart rebinds quickly
////        }
////    }
//    // MainActivity.kt — (re)bind on resume and when opened from notification
//    override fun onStart() {
//        super.onStart()
//        // if service is already running, this just binds; if not, start+bind if that's your policy
//        if (!isBound) {
//            val i = Intent(this, LocationService::class.java)
//            bindService(i, connection, BIND_AUTO_CREATE)
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (isBound) {
//            unbindService(connection)
//            isBound = false
//            boundService = null
//        }
//    }
//
//    // Called when opening via notification while activity already exists
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        // (optional) handle extras like sessionId here
//        if (!isBound) {
//            val i = Intent(this, LocationService::class.java)
//            bindService(i, connection, BIND_AUTO_CREATE)
//        }
//    }
//
//}

// MainActivity.kt (refactor)
class MainActivity : ComponentActivity() {

    private val TAG = "tanmay"

    private lateinit var viewModel: TrackingViewModel
    private var boundService: LocationService? = null
    private var isBound = false
    private var updatesJob: Job? = null

    // Queue one-shot work that should run as soon as we're bound
    private var pendingAction: ((LocationService) -> Unit)? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            val binder = service as LocationService.LocalBinder
            boundService = binder.getService()
            isBound = true

            // Start (or restart) the continuous collector for live UI updates
            updatesJob?.cancel()
            updatesJob = lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
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

        // DB/Repo/VM wiring
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "geo_tracker_db"
        ).build()
        val repo = LocationRepository(db.locationDao())
        viewModel = TrackingViewModel(repo, applicationContext)

        lifecycleScope.launch() {

        }
        handleIntentForSession(intent)

        setContent {
            GeoTrackerTheme {
                TrackingScreen(
                    viewModel = viewModel,
                    // 👉 START/RESUME continuous tracking
                    startOrStopService = {

                        val intent = Intent(this, LocationService::class.java)
                        // Start in STARTED mode first so it survives when Activity unbinds/minimizes
                        ContextCompat.startForegroundService(this, intent)
                        // Then bind for UI streaming
                        bindService(intent, connection, Context.BIND_AUTO_CREATE)
                        // optional: also command the service to begin emitting
                        // (only if your service requires an explicit startUpdates call)
                        pendingOrNow { svc -> svc.startLocationUpdates() }
                    },
                    // 👉 ONE-SHOT: get current location on demand
                    onMyLocationClick = {
                        pendingOrNow { svc ->
                            lifecycleScope.launch {
                                svc.fetchCurrentLocation()?.let { locEntity ->
                                    viewModel.handleNewLocation(locEntity)
                                }
                            }
                        }
                        // ensure service exists & bound (no-op if already)
                    }
                )

//                startAndBindService()
            }
        }

        // If you still run a permission gate, call it here; do NOT auto-start/bind in onStart.
        ensurePermissionsIfYouNeedTo()
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
//        ContextCompat.startForegroundService(this, intent)
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
            startAndBindService()
        }
    }

    private fun handleIntentForSession(intent: Intent?) {
        Log.d(TAG, "handleIntentForSession: $intent")
        val sid = intent?.getLongExtra("EXTRA_SESSION_ID", -1L) ?: -1L
        if (sid > 0) viewModel.openSession(sid)
    }
    // If you want reopen-from-notification to rebind (without auto-starting a new service):
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: $isBound")
        if (!isBound) {
            // Only bind; do NOT call startForegroundService here
            bindService(
                Intent(this, LocationService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    // ---- Permissions (keep whatever you had; just don't auto-start service here) ----
    private fun ensurePermissionsIfYouNeedTo() {
        // no-op in this refactor; keep your existing permission flow
    }
}
