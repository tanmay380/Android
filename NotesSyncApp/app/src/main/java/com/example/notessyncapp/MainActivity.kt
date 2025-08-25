package com.example.notessyncapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable // Added for potential future use
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController // Added import
import com.example.notessyncapp.navigation.NotesAppNavigation
import com.example.notessyncapp.navigation.NotesNavigationScreens // Added import
import com.example.notessyncapp.ui.theme.NotesSyncAppTheme
import dagger.hilt.android.AndroidEntryPoint

//import dagger.hilt.android.AndroidEntryPoint // Added if you use Hilt

@AndroidEntryPoint // Add this if you are using Hilt, remove if not
class MainActivity : ComponentActivity() {

    val TAG = "tanmay"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NotesSyncAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    val navController = rememberNavController()
                    NotesAppNavigation(
                        navController = navController,
                        startDestination = NotesNavigationScreens.SPLASHSCREEN.name
                    )
                }
            }
        }
    }
}
