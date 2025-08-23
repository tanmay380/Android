package com.example.notesapp.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesapp.MainActivity.Companion.TAG
import com.example.notesapp.R
import com.example.notesapp.components.NoteInputText
import com.example.notesapp.components.NotesButton
import com.example.notesapp.data.NoteDataSource
import com.example.notesapp.model.Note
import com.example.notesapp.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    notes: List<Note>,
    onAddNote: (Note) -> Unit = {},
    onRemoveNote: (Note) -> Unit = {},
    onNoteLongClick: (Note) -> Unit
) {
    val noteViewModel = viewModel<NoteViewModel>()
    
//    var title by remember {
//        mutableStateOf("")
//    }

    val text = noteViewModel.name
    var description by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(6.dp)) {
        TopAppBar(title = {
            Text(text = stringResource(R.string.app_name))
        }, actions = {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Icon"
            )
        },
            colors = topAppBarColors(containerColor = Color.Cyan)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NoteInputText(modifier = Modifier.padding(top = 9.dp, bottom = 8.dp),
                text = text.value,
                label = "Title",
                onTextChange = {
                    if (it.all { char ->
                            char.isLetter() || char.isWhitespace()
                        }) {

                        Log.d(TAG, "NotesScreen: " + it)
                        noteViewModel.setTitle(it)
                    }
                })
            NoteInputText(modifier = Modifier.padding(top = 9.dp, bottom = 8.dp),
                text = description,
                label = "Add a note",
                onTextChange = {
                    if (it.all { char ->
                            char.isLetter() || char.isWhitespace()
                        }) description = it
                })
            NotesButton(modifier = Modifier.padding(top = 9.dp, bottom = 8.dp),
                text = "Add a Note", onClick = {
                    if (noteViewModel.name.value.isNotEmpty() && description.isNotEmpty()) {
                        onAddNote(
                            Note(
                                title = noteViewModel.name.value,
                                description = description
                            )
                        )
                        noteViewModel.setTitle("")
                        description = ""
                        Toast.makeText(
                            context,
                            "Note Added",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
        HorizontalDivider(modifier = Modifier.padding(10.dp))
        //TODO: Display all notes
        LazyColumn() {
            items(notes) { notes ->
                NoteRow(note = notes,
                    onNoteClick = { note -> onRemoveNote(note) },
                    onNoteLongClick = { noteLonClick -> onNoteLongClick(noteLonClick) }
                )
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteRow(
    modifier: Modifier = Modifier,
    note: Note,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit
) {
    Surface(
        modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(topEnd = 33.dp, bottomStart = 33.dp))
            .fillMaxWidth(),
        color = Color(0xFFDFE6EB)
    ) {
        Column(
            modifier
//                .clickable {onNoteClick(note) }
                .combinedClickable(
                    onClick = { onNoteClick(note) },
                    onLongClick = { onNoteLongClick(note) })
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = note.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatDate(note.entryDate.time),
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    NotesScreen(notes = NoteDataSource().loadNotes(),
        onAddNote = {},
        onRemoveNote = {},
        onNoteLongClick = {})
}