package com.example.weathercomposeapp.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun RowWithImageAndText(image: ImageVector, text: String, content: String) {
    Row {
        Image(
            imageVector = image, contentDescription = content,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            modifier = Modifier.padding(2.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
