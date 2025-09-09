package com.example.geotracker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime

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

    fun bitmapDescriptorFromVector(context: Context, drawableId: Int, width: Int? = 1, height: Int? = 1): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(vectorToBitmap(context, drawableId, width, height))
    }

    @OptIn(ExperimentalTime::class)
    fun formatEpoch(ms: Long): String {
        val instant = Instant.ofEpochMilli(ms)
        val zone = ZoneId.systemDefault() // phone's timezone
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return instant.atZone(zone).format(formatter)
    }


    private fun vectorToBitmap(context: Context, drawableId: Int, width: Int?, height: Int?): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)!!
        val bitmap = createBitmap((drawable.intrinsicWidth / width!!), drawable.intrinsicHeight / height!!)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

