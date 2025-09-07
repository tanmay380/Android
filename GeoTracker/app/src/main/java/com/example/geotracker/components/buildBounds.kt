package com.example.geotracker.components

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** Build LatLngBounds including all points. If only one point, create a small bounds around it. */
fun buildBounds(points: List<LatLng>): LatLngBounds? {
    if (points.isEmpty()) return null
    if (points.size == 1) {
        val p = points.first()
        // small offset ~ 0.001 deg ≈ 100m (roughly) to avoid zero-area bounds
        val delta = 0.001
        return LatLngBounds(
            LatLng(p.latitude - delta, p.longitude - delta),
            LatLng(p.latitude + delta, p.longitude + delta)
        )
    }

    val builder = LatLngBounds.builder()
    points.forEach { builder.include(it) }
    return builder.build()
}

/** compute padding in pixels based on screen size; uses 10-18% as a comfortable margin */
@Composable
fun defaultMapPaddingPx(): Int {
    val conf = LocalConfiguration.current
    val density = LocalDensity.current
    val widthPx = with(density) { conf.screenWidthDp.dp.toPx() }
    val heightPx = with(density) { conf.screenHeightDp. dp.toPx() }
    // Use smaller dimension to base padding so route is not clipped on narrow dimension.
    val base = min(widthPx, heightPx)
    // padding ratio — tweak 0.12..0.20 to taste
    val padding = (base * 0.15f).toInt()
    return padding
}
