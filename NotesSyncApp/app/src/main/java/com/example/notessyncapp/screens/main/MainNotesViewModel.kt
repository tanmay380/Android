package com.example.notessyncapp.screens.main

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notessyncapp.model.Notes
import com.example.notessyncapp.repository.NotesRepository
import com.example.notessyncapp.repository.NotesRepositoryFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainNotesViewModel @Inject constructor(private val notesRepositoryfactory: NotesRepositoryFactory): ViewModel() {
    private val _notesList = MutableStateFlow<List<Notes>>(emptyList())
    val notesList = _notesList.asStateFlow()

    private var notesRepository: NotesRepository
    init {
        notesRepository = notesRepositoryfactory.createNotesRepository("")
        Log.d(TAG, "init fies: ")
        viewModelScope.launch {
            notesRepository.getAllNotes().distinctUntilChanged()
                .collect{
                    _notesList.value = it

//                    Log.d(TAG, "notes are : $it")
                }

            Log.d(TAG, "init fies: end $notesList")
        }
    }

    fun getAllNotes() = notesRepository.getAllNotes()

}