package com.example.geotracker.navigation

import DetailsScreen
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.location.service.LocationService
import com.example.geotracker.screen.TrackingScreen
import com.example.geotracker.screen.TrackingViewModel
import com.example.permissions.PermissionGateSequential
import kotlinx.serialization.Serializable

@Composable
fun GeoTrackerNavigation(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController,
        startDestination
    ) {

        navigation(
            "Main Screen",
            startDestination
        ) {
            composable("Main Screen") {
                val viewModel: TrackingViewModel = hiltViewModel()
                PermissionGateSequential {
                    if (ActivityCompat.checkSelfPermission(
                            LocalContext.current,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                    }
                    TrackingScreen(
                        viewModel = viewModel,
                        navController
                    )
                }
            }
            composable("Details Screen") {
                Log.d(TAG, "GeoTrackerNavigation: " +
                        "${navController.previousBackStackEntry?.savedStateHandle?.get<Set<Long>>("selectedId")?.size}")
                DetailsScreen()
            }
        }
    }
}
