package com.example.geotracker.model

import com.google.android.gms.maps.model.LatLng

data class RoutePoint(
    val lat: Double,
    val lng: Double,
    val speed: Float,     // m/s or km/h as you store it
    val timestamp: Long  ,
    val distanceClicked: Float = 0f// optional
) {
    fun toLatLng() = LatLng(lat, lng)
}
