package com.example.geotracker.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geotracker.model.RoutePoint
import com.example.geotracker.utils.Utils
import com.example.geotracker.utils.Utils.formatDuration
import kotlinx.coroutines.delay

@Composable
fun ElapsedTimerText(sessionStartMs: Long): String {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
// Log.d(TAG, "TrackingScreen session vlue: ${sessionStartMs}")


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
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Lat: ${"%.6f".format(selected.lat)}")
            Text("Lng: ${"%.6f".format(selected.lng)}")
            val speed = "%.1f".format(selected.speed).toFloat()
            Text("Speed: $speed kmph")
            Text("time : ${Utils.formatEpoch(selected.timestamp)} ")
            Text("distance: ${selected.distanceClicked}")
// add timestamp etc.
        }
    }
}
