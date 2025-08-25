package com.example.weathercomposeapp.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weathercomposeapp.screens.about.WeatherAboutScreen
import com.example.weathercomposeapp.screens.favorites.WeatherFavoritesScreen
import com.example.weathercomposeapp.screens.settings.WeatherSettingsScreen
import com.example.weathercomposeapp.screens.main.MainScreen
import com.example.weathercomposeapp.screens.main.MainViewModel
import com.example.weathercomposeapp.screens.search.SeachScreen
import com.example.weathercomposeapp.screens.splash.WeatherSplashScreen

@Composable
fun WeatherNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController,
        WeatherScreens.SplashScreen.name
    ) {

        composable(WeatherScreens.SplashScreen.name) {
            WeatherSplashScreen(navController = navController)
        }
        val route = WeatherScreens.MainScreen.name
        composable(
            "$route/{city}",
            arguments = listOf(
                navArgument(name = "city") {
                    type = NavType.StringType
                }
            )) { navBack ->
            navBack.arguments?.getString("city").let {
                val mainViewModel = hiltViewModel<MainViewModel>()
                MainScreen(navController = navController, mainViewModel, it)
            }
        }

        composable(WeatherScreens.SearchScreen.name) {
//            val mainViewModel = hiltViewModel<MainViewModel>()
            SeachScreen(navController = navController)
        }
        composable(WeatherScreens.AboutScreen.name) {
//            val mainViewModel = hiltViewModel<MainViewModel>()
            WeatherAboutScreen(navController = navController)
        }
        composable(WeatherScreens.FavScreen.name) {
//            val mainViewModel = hiltViewModel<MainViewModel>()
            WeatherFavoritesScreen(navController = navController)
        }
        composable(WeatherScreens.SettingsScreen.name) {
//            val mainViewModel = hiltViewModel<MainViewModel>()
            WeatherSettingsScreen(navController = navController)
        }

    }

}
