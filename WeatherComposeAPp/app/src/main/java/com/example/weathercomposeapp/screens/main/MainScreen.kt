package com.example.weathercomposeapp.screens.main

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weathercomposeapp.data.DataOrException
import com.example.weathercomposeapp.model.Weather
import com.example.weathercomposeapp.navigation.WeatherScreens
import com.example.weathercomposeapp.utils.formatDate
import com.example.weathercomposeapp.widgets.HumidityDetails
import com.example.weathercomposeapp.widgets.SunRiseSunSet
import com.example.weathercomposeapp.widgets.WeatherAppBar
import com.example.weathercomposeapp.widgets.WeatherDetailRow
import com.example.weathercomposeapp.widgets.WeatherImage

@Composable
fun MainScreen(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    string: String?
) {
    Log.d("tanmay", "MainScreen: $string")
    val weatherData = produceState<DataOrException<Weather, Boolean, Exception>>(
        initialValue = DataOrException(boolean = true)
    ) {
        value = mainViewModel.getWeatherData(string.toString())
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
                title = weather.location.name, elevtion = 50.dp,
                country = weather.location.country,
                navController = navController,
                onAddActionClicked = {
                    navController.navigate(WeatherScreens.SearchScreen.name)
                }
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
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,

                    )
            }
        }
        HumidityDetails(data.current)
        HorizontalDivider()
        SunRiseSunSet(data.forecast.forecastday[0].astro)
        Text(
            "This Week",
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.padding(5.dp))
        HorizontalDivider()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFD1ECD4),
            shape = RoundedCornerShape(14.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(2.dp),
                contentPadding = PaddingValues(1.dp)
            ) {
                items(data.forecast.forecastday) {
                    WeatherDetailRow(it)
                }
            }
        }
    }
}

