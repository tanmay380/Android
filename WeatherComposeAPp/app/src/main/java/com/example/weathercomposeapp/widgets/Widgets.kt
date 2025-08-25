package com.example.weathercomposeapp.widgets

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.weathercomposeapp.components.RowWithImageAndText
import com.example.weathercomposeapp.model.Astro
import com.example.weathercomposeapp.model.Current
import com.example.weathercomposeapp.model.Forecastday
import com.example.weathercomposeapp.utils.Blood_pressure
import com.example.weathercomposeapp.utils.Rainy
import com.example.weathercomposeapp.utils.Sunrise
import com.example.weathercomposeapp.utils.Sunset
import com.example.weathercomposeapp.utils.formatDate

@Composable
fun WeatherDetailRow(data: Forecastday) {
    val imageUrl = data.day.condition.icon
    Surface(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth(),
        shape = CircleShape.copy(topEnd = CornerSize(9.dp)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatDate(data.date_epoch)
                    .split(",")[0],
                modifier = Modifier.padding(5.dp),
                fontSize = 20.sp
            )
            WeatherImage(imageUrl, modifier = Modifier.size(56.dp))

            Surface(
                modifier = Modifier.padding(1.dp),
                shape = CircleShape,
                color = Color(0xFFFFC400)
            ) {
                Text(
                    data.day.condition.text,
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(text = data.day.maxtemp_c.toString()+"°")
                }
                withStyle(
                    style = SpanStyle(
                        color = Color.LightGray,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(text = data.day.mintemp_c.toString()+"°",)
                }
            }
            )
        }
    }
}

@Composable
fun SunRiseSunSet(data: Astro) {
    Row(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RowWithImageAndText(Sunrise, data.sunrise, "sunrise icon")
        Row {
            Text(
                text = data.sunset,
                modifier = Modifier.padding(2.dp),
                style = MaterialTheme.typography.bodySmall
            )
            Image(
                imageVector = Sunset, contentDescription = "sunset icon",
                modifier = Modifier.size(20.dp)
            )
        }

    }
}


@Composable
fun HumidityDetails(data: Current) {
    Row(
        modifier = Modifier
            .padding(15.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RowWithImageAndText(Rainy, data.humidity.toString() + "%", "rain")
        RowWithImageAndText(Blood_pressure, data.pressure_mb.toString() + "psi", "pressure Icon")
        RowWithImageAndText(Rainy, data.wind_kph.toString() + "kmph", "Wind")
    }
}


@Composable
fun WeatherImage(url: String, modifier: Modifier = Modifier) {
//    Log.d("tanmay", "WeatherImage: $url")
    Image(
        painter = rememberAsyncImagePainter("https:$url"),
        contentDescription = "icon image",
        modifier = modifier.size(80.dp)
    )
}

