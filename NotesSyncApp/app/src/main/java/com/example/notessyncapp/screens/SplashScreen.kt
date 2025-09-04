package com.example.notessyncapp.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.notessyncapp.navigation.NotesNavigationScreens
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2000L) // Delay for 2 seconds
        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            Log.d("SplashScreen", "User logged in, navigating to MAINSCREEN")
            navController.navigate(NotesNavigationScreens.MAINSCREEN.name) {
                popUpTo(NotesNavigationScreens.SPLASHSCREEN.name) { inclusive = true }
            }
        } else {
            Log.d("SplashScreen", "No user, navigating to LOGINSIGNUPSCREEN")
            navController.navigate(NotesNavigationScreens.LOGINSIGNUPSCREEN.name) {
                popUpTo(NotesNavigationScreens.SPLASHSCREEN.name) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Notes App Sync Spalsh Screen")
    }
}
