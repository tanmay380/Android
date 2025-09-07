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

    fun bitmapDescriptorFromVector(context: Context, drawableId: Int): BitmapDescriptor {
        return BitmapDescriptorFactory.fromBitmap(vectorToBitmap(context, drawableId))
    }


    private fun vectorToBitmap(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)!!
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

