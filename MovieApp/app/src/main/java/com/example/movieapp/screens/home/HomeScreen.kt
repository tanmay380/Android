package com.example.movieapp.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.movieapp.model.Movie
import com.example.movieapp.model.getMovies
import com.example.movieapp.navigation.MovieScreens
import com.example.movieapp.widgets.MovieRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .padding(4.dp)
        .windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            TopAppBar(
                title = {
                    Text("Movie App")
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Magenta)
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {

            MainContent(navController)

        }
    }
}

@Composable
fun MainContent(
    navController: NavController,
    movieList: List<Movie> = getMovies()
) {
    LazyColumn (modifier = Modifier.padding(top = 5.dp, end = 15.dp, bottom = 10.dp, start = 15.dp
    )){
        items(items = movieList) {
            MovieRow(it) { movie ->
                navController.navigate(route = MovieScreens.DetailsScreen.name + "/$movie")
            }
        }
    }
}