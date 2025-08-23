package com.example.movieapp.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.movieapp.model.getMovies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(navController: NavController, movieId: String?) {
    val movie = getMovies().find {
        it.id == movieId
    }
    Scaffold(modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars), topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.LightGray),
            title = { Text("Movies", modifier = Modifier.offset(x = 50.dp)) },
            navigationIcon = {
                IconButton(onClick = {
                    navController.navigateUp()
                }) {
                    Row {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                }
            })
    }) {
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = movie!!.title,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.systemBars
                    ),
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
    }
}