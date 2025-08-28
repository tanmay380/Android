package com.example.notessyncapp.screens.addNote

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notessyncapp.components.InputText
import com.example.notessyncapp.model.Notes
import java.util.Date
import java.time.Instant


@Composable
fun AddNotes(
    navController: NavController,
    viewModel: AddNoteViewModel = hiltViewModel(),
    /*id: String? = null*/
) {
    val isUpdating by viewModel.isUpdating.collectAsState()
//    val noteState by viewModel.noteState.collectAsState()
    val updateNotes by viewModel.updateNotes.collectAsState()
    val id by viewModel.noteId.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    Log.d("tanmay", "AddNotes: $isUpdating $id")

//    LaunchedEffect(noteState) {
//        title = noteState?.title ?: ""
//        description = noteState?.description ?: ""
//    }


    Surface(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InputText(
                text = title,
                label = "Title",
                onImeAction = ImeAction.Next,
                maxLines = 1,
                onTextChange = {
                    Log.d("tanmay", "AddNotes: $id")
                    viewModel.setTitle(it)
                    viewModel.autoSave( id, it, description)
                })
            InputText(text = description, label = "Notes", onTextChange = {
                viewModel.setDescription(it)
                viewModel.autoSave(id, title, it)
            })
            Button(modifier = Modifier.padding(5.dp), onClick = {
                Log.d("tanmay", "update bnotes value: $updateNotes")
                val notes = updateNotes?.copy(
                    title = title,
                    description = description,
                    entryTime = Date.from(Instant.now())
                ) ?:
                Notes(
                    title = title,
                    description = description
                )
                Log.d("tanmay", "AddNotes: $notes")

                if (!isUpdating)
                    viewModel.saveNotes(
                        notes
                    )
                else {
                    notes.id = id?: notes.id
                    viewModel.updateNotes(notes)
                }
                navController.navigateUp()
            }) {
                Text(text = "Save")
            }
        }
    }
}
