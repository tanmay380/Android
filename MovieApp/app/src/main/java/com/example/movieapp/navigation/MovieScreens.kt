package com.example.movieapp.navigation

import android.util.Log
import kotlin.math.log

enum class MovieScreens{
    HomeScreen,
    DetailsScreen;
    companion object {
        fun fromRoute(route: String?): MovieScreens {
           return when (route?.substringBefore("/")) {
                HomeScreen.name -> HomeScreen
                DetailsScreen.name -> DetailsScreen
                null -> HomeScreen
                else -> throw IllegalArgumentException("Route $route is not recognized")
            }
        }
    }
}