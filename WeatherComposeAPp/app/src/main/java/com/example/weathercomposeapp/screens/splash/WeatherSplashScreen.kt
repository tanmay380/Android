package com.example.weathercomposeapp.screens.splash

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weathercomposeapp.R
import com.example.weathercomposeapp.navigation.WeatherScreens
import kotlinx.coroutines.delay

@Composable
fun WeatherSplashScreen(navController: NavController) {
    val scale = remember {
        Animatable(0f)
    }

    LaunchedEffect(true, block = {
        scale.animateTo(targetValue = 0.9f,
            animationSpec = tween(800,
                easing = {
                    OvershootInterpolator(8f)
                        .getInterpolation(it)
                })
        )
        delay(1000L)
        navController.navigate(WeatherScreens.MainScreen.name+"/New Delhi")
    })


    Surface(
        modifier = Modifier
            .padding(15.dp)
            .scale(scale.value)
            .size(330.dp),
        shape = CircleShape,
        border = BorderStroke(
            width = 2.dp,
            color = Color.LightGray
        )
    ) {
        Column (modifier = Modifier.padding(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center){
            Image(painter = painterResource( R.drawable.baseline_sunny_24),
                contentDescription = "splash screen sun",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit)
            Text("Find the Sun?",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.LightGray)
        }
    }
}