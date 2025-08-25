package com.example.weathercomposeapp.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.weathercomposeapp.R
import com.example.weathercomposeapp.widgets.WeatherAppBar


@Composable
fun WeatherAboutScreen(navController: NavController){
    Scaffold(topBar ={ WeatherAppBar(
        title = "About",
        icon = Icons.Default.ArrowBack,
        isMainScreen = false,
        navController = navController
    ){
        navController.navigateUp()
    }
        }
    ) {
        Surface(modifier = Modifier.padding(it)
            .fillMaxWidth()
            .fillMaxHeight()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text(text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold)

                Text(text = "Open Weather api Used",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold)
            }

        }
    }
}
