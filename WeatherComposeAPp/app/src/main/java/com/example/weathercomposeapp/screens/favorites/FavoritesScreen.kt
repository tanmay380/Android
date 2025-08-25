package com.example.weathercomposeapp.screens.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weathercomposeapp.model.Favorites
import com.example.weathercomposeapp.navigation.WeatherScreens
import com.example.weathercomposeapp.widgets.WeatherAppBar

@Composable
fun WeatherFavoritesScreen(
    navController: NavController,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    Scaffold(modifier = Modifier.fillMaxWidth(),
        topBar = {
            WeatherAppBar(
                title = "Favorites",
                isMainScreen = false,
                icon = Icons.Default.ArrowBack,
                navController = navController
            ) {
                navController.navigateUp()
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val list = favoritesViewModel.favList.collectAsState().value

                LazyColumn {
                    items(list) {
                        CityRow(it, navController, favoritesViewModel)
                    }

                }
            }
        }
    }
}

@Composable
fun CityRow(
    favorites: Favorites,
    navController: NavController,
    favoritesViewModel: FavoritesViewModel
) {
    Surface(
        Modifier
            .padding(3.dp)
            .fillMaxWidth()
            .height(50.dp)
            .clickable {
                navController.navigate(
                    WeatherScreens.MainScreen.name+"/${favorites.city}"
                )
            },
        shape = CircleShape.copy(topEnd = CornerSize(6.dp))
    ) {
        Row(
            Modifier
                .padding()
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = favorites.city, modifier = Modifier.padding(4.dp).width(150.dp),
                maxLines = 3)

            Surface(
                Modifier.padding(0.dp),
                shape = CircleShape,
                color = Color.LightGray
            ) {
                Text(
                    favorites.country,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                        .width(130.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.Delete, contentDescription = "",
                modifier = Modifier.clickable {
                    favoritesViewModel.deleteFavorite(favorites)
                },
                tint = Color.Red.copy(alpha = 0.3f)
            )
        }
    }
}