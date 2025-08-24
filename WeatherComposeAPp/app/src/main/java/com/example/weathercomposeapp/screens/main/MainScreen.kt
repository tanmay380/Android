package com.example.weathercomposeapp.screens.main

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.weathercomposeapp.components.RowWithImageAndText
import com.example.weathercomposeapp.data.DataOrException
import com.example.weathercomposeapp.model.Astro
import com.example.weathercomposeapp.model.Current
import com.example.weathercomposeapp.model.Forecastday
import com.example.weathercomposeapp.model.Weather
import com.example.weathercomposeapp.utils.Blood_pressure
import com.example.weathercomposeapp.utils.Rainy
import com.example.weathercomposeapp.utils.Sunrise
import com.example.weathercomposeapp.utils.Sunset
import com.example.weathercomposeapp.utils.formatDate
import com.example.weathercomposeapp.widgets.WeatherAppBar
import java.nio.file.WatchEvent

@Composable
fun MainScreen(navController: NavController, mainViewModel: MainViewModel = hiltViewModel()) {
    val weatherData = produceState<DataOrException<Weather, Boolean, Exception>>(
        initialValue = DataOrException(boolean = true)
    ) {
        value = mainViewModel.getWeatherData("London")
    }.value
    if (weatherData.boolean == true) {
        CircularProgressIndicator()
    } else if (weatherData.data != null) {
        MainScaffold(weather = weatherData.data!!, navController)
    }
}

@Composable
fun MainScaffold(weather: Weather, navController: NavController) {
    Scaffold(
        modifier = Modifier
            .padding(4.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            WeatherAppBar(
                title = "Helena", elevtion = 50.dp,
                navController = navController,
                icon = Icons.Default.ArrowBack
            ) {
                Log.d("tanmay", "MainScaffold: button cliocked")
            }
        },
    ) {
        MainContent(
            modifier = Modifier.padding(it),
            data = weather
        )
    }

}


@Composable
fun MainContent(modifier: Modifier, data: Weather) {
    val weatherImage = "${data.current.condition.icon}"
    Column(
        modifier
            .padding(4.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatDate(data.forecast.forecastday.get(0).date_epoch),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(6.dp)
        )

        Surface(
            modifier = Modifier
                .padding(4.dp)
                .size(200.dp),
            shape = CircleShape,
            color = Color.Yellow
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WeatherImage(weatherImage)
                Text(
                    text = data.current.temp_c.toString() + "°",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = data.current.condition.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
        HumidityDetails(data.current)
        HorizontalDivider()
        SunRiseSunSet(data.forecast.forecastday[0].astro)
        Text("This Week",
            fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.padding(5.dp))
        HorizontalDivider()

        Surface(modifier = Modifier.fillMaxSize(),
            color = Color(0xFFD1ECD4),
            shape = RoundedCornerShape(14.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(2.dp),
                contentPadding = PaddingValues(1.dp)) {
                items(data.forecast.forecastday){
                    WeatherDetailRow(it)
                }
            }
        }
    }
}
//@Preview
@Composable
fun WeatherDetailRow(data: Forecastday) {
    val imageUrl = data.day.condition.icon
    Surface(
        modifier = Modifier.padding(5.dp)
            .fillMaxWidth(),
        shape = CircleShape.copy(topEnd = CornerSize(6.dp)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatDate(1755993600)
                .split(",")[0],
                fontSize = 20.sp)
            WeatherImage(imageUrl)
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
            modifier = Modifier.size(20.dp))
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
fun WeatherImage(url: String) {
    Log.d("tanmay", "WeatherImage: $url")
    Image(
        painter = rememberAsyncImagePainter("https:$url"),
        contentDescription = "icon image",
        modifier = Modifier.size(80.dp)
    )
}

