package com.example.notesapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.notesapp.screen.NoteViewModel
import com.example.notesapp.screen.NotesScreen
import com.example.notesapp.ui.theme.NotesAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        val TAG: String = "tanmay"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesAppTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) { inner ->
                    val notesViewModel: NoteViewModel by viewModels()
//                    val notesViewModel = viewModel<NoteViewModel>()
                    NotesApp(
                        modifier = Modifier.padding(inner),
                        notesViewModel = notesViewModel
                    )

                }
            }
        }
    }
}

@Composable
fun NotesApp(notesViewModel: NoteViewModel, modifier: Modifier = Modifier) {
    val notesList = notesViewModel.noteList.collectAsState().value

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NotesScreen(
            notes = notesList,
            onAddNote = {
                notesViewModel.addNote(it)
            },
            onRemoveNote = {
                notesViewModel.deleteNote(it)
            },
            onNoteLongClick = {
//                description = it.description
                notesViewModel.name.value = it.title
            })

//        Log.d(TAG, "NotesApp: " + title + description)
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotesAppTheme {
//        NotesApp(notesViewModel = null)
    }
}