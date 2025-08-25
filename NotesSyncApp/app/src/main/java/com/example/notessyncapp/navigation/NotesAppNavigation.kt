package com.example.notessyncapp.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.notessyncapp.screens.SplashScreen
import com.example.notessyncapp.screens.addNote.AddNoteViewModel
import com.example.notessyncapp.screens.addNote.AddNotes
import com.example.notessyncapp.screens.main.MainNotesScreen
import com.example.notessyncapp.screens.signInUp.LoginScreen
import com.example.notessyncapp.screens.signInUp.LoginViewModel
import kotlinx.serialization.Serializable

@Composable
fun NotesAppNavigation(
    navController: NavHostController,
    startDestination: String // This will be SPLASHSCREEN.name from MainActivity
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NotesNavigationScreens.SPLASHSCREEN.name) {
            SplashScreen(navController = navController) // Pass navController
        }

        // Authentication Graph
        navigation(
            startDestination = NotesNavigationScreens.LOGINSIGNUPSCREEN.name,
            route = NotesNavigationScreens.AUTH_GRAPH.name
        ) {
            composable(NotesNavigationScreens.LOGINSIGNUPSCREEN.name) {
                // If you are using Hilt for LoginViewModel:
                val loginViewModel: LoginViewModel = viewModel()
                // If you are not using Hilt for LoginViewModel:
                // val loginViewModel: LoginViewModel = viewModel()

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        Log.d("NotesAppNavigation", "Login successful, navigating to MAIN_GRAPH")
                        navController.navigate(NotesNavigationScreens.MAIN_GRAPH.name) {
                            popUpTo(NotesNavigationScreens.AUTH_GRAPH.name) { // Pop the entire auth graph
                                inclusive = true
                            }
                            launchSingleTop = true // Avoid multiple copies of the main graph
                        }
                    }
                )
            }
            // You can add other auth-related screens here later, like a SignUpScreen
            // composable(NotesNavigationScreens.SIGNUPSCREEN.name) { /* ... */ }
        }

        // Main Application Graph
        navigation(
            startDestination = NotesNavigationScreens.MAINSCREEN.name,
            route = NotesNavigationScreens.MAIN_GRAPH.name
        ) {
            composable(NotesNavigationScreens.MAINSCREEN.name) {
                MainNotesScreen(navController = navController)
            }
//            composable(NotesNavigationScreens.ADD_NOTES.name+"?noteId={noteId}") {
//
//                AddNotes(navController = navController)
//            }
            composable<AddNotesScreenClass> {
                val args = it.toRoute<AddNotesScreenClass>()
                AddNotes(navController = navController/*, id = args.hello*/)
            }


            // You can add other main app screens here later, like NoteDetailScreen
            // composable("note_detail_route/{noteId}") { /* ... */ }
        }
    }
}


@Serializable
data class AddNotesScreenClass(
    val hello: String?
)
