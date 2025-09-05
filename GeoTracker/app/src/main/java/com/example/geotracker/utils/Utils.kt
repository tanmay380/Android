package com.example.geotracker.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    fun getTimeElapsed(timestamp: Long): String{
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timestamp

        val date = Date(elapsedTime)
        // Define the desired date format
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        // Format the date and return as String

        Log.d("tanmay", "getTimeElapsed: ${sdf.format(date)}  $elapsedTime")
        return sdf.format(date)

    }
    fun formatDuration(ms: Long): String {
        val totalSec = (ms.coerceAtLeast(0)) / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }
}