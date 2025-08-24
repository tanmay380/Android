package com.example.weathercomposeapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

fun formatDate(timeStamp: Int): String {
    val sdf = SimpleDateFormat("EEE, MM d")
    val date = Date(timeStamp.toLong()*1000)

    return sdf.format(date)
}

