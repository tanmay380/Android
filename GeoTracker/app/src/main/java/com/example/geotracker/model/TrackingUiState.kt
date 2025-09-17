package com.example.geotracker.model

import com.google.android.gms.maps.model.LatLng

data class TrackingUiState(
    val currentLatLng: LatLng = LatLng(0.0, 0.0),
    val routePoints: List<LatLng> = emptyList(), // will only track if the service is active.
    val displayPolylines: List<List<LatLng>> = emptyList(), //SINGLE SOURCE OF TRUTH FOR the route if serviec is not running.
    val distance: Double = 0.0,
    val speed: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val locationPermissionGranted: Boolean = false,
    val sessionStartMs: Long = 0L,
    val bearing: Float = 0f,
    val elevation: String = "",
    val accuracy: Int = 0
)
