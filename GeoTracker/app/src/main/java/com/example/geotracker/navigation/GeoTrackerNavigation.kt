package com.example.geotracker.navigation

import DetailsScreen
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.geotracker.screen.screens.TrackingScreen
import com.example.geotracker.screen.viewmodel.SharedViewModel
import com.example.geotracker.screen.viewmodel.TrackingViewModel
import com.example.permissions.PermissionGateSequential

@Composable
fun GeoTrackerNavigation(
    navController: NavHostController,
    startDestination: String,
    viewModel: TrackingViewModel,
    sharedViewModel: SharedViewModel
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
                PermissionGateSequential {
                    if (ActivityCompat.checkSelfPermission(
                            LocalContext.current,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                    }
                    TrackingScreen(
                        viewModel = viewModel,
                        navController,
                        sharedViewModel
                    )
                }
            }
            composable("Details Screen") {
                val sharedViewModel: SharedViewModel = hiltViewModel()
                DetailsScreen(
                    sharedViewModel = sharedViewModel,
                    navController = navController
                )
            }
        }
    }
}
