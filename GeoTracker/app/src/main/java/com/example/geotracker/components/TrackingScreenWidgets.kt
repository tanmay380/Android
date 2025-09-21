package com.example.geotracker.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geotracker.MainActivity.Companion.TAG
import com.example.geotracker.model.RoutePoint
import com.example.geotracker.utils.Utils
import com.example.geotracker.utils.Utils.formatDuration
import kotlinx.coroutines.delay

/*fun ElapsedTimerText(sessionStartMs: Long): String {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
 Log.d(TAG, "TrackingScreen session vlue: Elpased Rimer text ${formatDuration(sessionStartMs)}")

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
}*/

@Composable
fun ElapsedTimerText(sessionStartMs: Long): State<String> {
    // internal 'now' that the timer coroutine updates
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
//    Log.d(TAG, "ElapsedTimerText: ${formatDuration(sessionStartMs)}")
    // restart coroutine when sessionStartMs changes
    LaunchedEffect(sessionStartMs) {
        // ensure immediate evaluation then align to next whole second
        now = System.currentTimeMillis()
        while (true) {
            // align next tick to the next second boundary to avoid drift
            val delayMs = 1000L - (now % 1000L)
            delay(delayMs)
            now = System.currentTimeMillis()
        }
    }

    // derived state that updates whenever `now` or `sessionStartMs` change
    return remember(now, sessionStartMs) {
        derivedStateOf {
            if (sessionStartMs > 0L) {
                val elapsed = now - sessionStartMs
                if (elapsed > 0L) formatDuration(elapsed) else "00:00:00"
            } else {
                "--"
            }
        }
    }
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
