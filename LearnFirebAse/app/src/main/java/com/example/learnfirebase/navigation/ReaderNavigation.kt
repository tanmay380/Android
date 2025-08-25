package com.example.learnfirebase.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.learnfirebase.screens.ReaderSplashScreen
import com.example.learnfirebase.screens.home.ReaderHomeScreen
import com.example.learnfirebase.screens.login.ReaderLoginScreen

@Composable
fun ReaderNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ReaderScreens.ReaderHomeScreen.name) {
        composable(ReaderScreens.SplashScreen.name) {
            ReaderSplashScreen(navController = navController)
        }
        composable(ReaderScreens.ReaderHomeScreen.name) {
            ReaderHomeScreen(navController = navController)
        }
        navigation(startDestination = ReaderScreens.LoginScreen.name, route = "auth") {
            composable(ReaderScreens.LoginScreen.name) {
                ReaderLoginScreen(navController = navController)
            }
        }
        navigation(startDestination = ReaderScreens.ReaderHomeScreen.name, route = "home") {
            composable(ReaderScreens.ReaderHomeScreen.name) {
                ReaderHomeScreen(navController = navController)
            }
        }
    }
}
