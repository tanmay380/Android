package com.example.notessyncapp.screens.main

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notessyncapp.model.Notes
import com.example.notessyncapp.navigation.AddNotesScreenClass
import com.example.notessyncapp.navigation.NotesNavigationScreens
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val TAG = "tanmay"

@Composable
fun MainNotesScreen(
    navController: NavController,
    viewModel: MainNotesViewModel = hiltViewModel()
) {
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = {
            Log.d(TAG, "MainNotesScreen: Add new Notes")
            navController.navigate(AddNotesScreenClass(null))
        }) {
            Text(text = "Add")
        }
    }) {Column {
        val list = viewModel.notesList.collectAsState().value
//        Log.d(TAG, "MainNotesScreen: $list")
        Button(modifier = Modifier.padding(it), onClick = {
            FirebaseAuth.getInstance().signOut()
            viewModel.deleteAllNotes()
            navController.navigate(NotesNavigationScreens.LOGINSIGNUPSCREEN.name)
        }) {
            Text("Sign Out")
        }
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {

            items(list) {
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable(onClick = {
//                        navController.navigate(NotesNavigationScreens.ADD_NOTES.name)
                            navController.navigate(AddNotesScreenClass(it.id.toString()))
                        })

                        .fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.LightGray
                ) {
                    Column {
                        Text(
                            it.title, style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(it.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

        }
    }

    }
}
