package com.example.notessyncapp.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notessyncapp.model.Notes
import com.example.notessyncapp.repository.NotesRepository
import com.example.notessyncapp.repository.NotesRepositoryFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainNotesViewModel @Inject constructor(private val notesRepositoryfactory: NotesRepositoryFactory) :
    ViewModel() {
    private val _notesList = MutableStateFlow<List<Notes>>(emptyList())
    val notesList = _notesList.asStateFlow()

    private var notesRepository: NotesRepository


    init {
        Log.d(TAG, "${FirebaseAuth.getInstance().currentUser!!.email}: ")
        notesRepository = notesRepositoryfactory.createNotesRepository("")
        Log.d(TAG, "init fies: ")
        viewModelScope.launch {
            Log.d(TAG, "inside launch")
            notesRepository.getAllNotes().distinctUntilChanged()
                .collect {

                    Log.d(TAG, "inside collect $notesList")
                    _notesList.value = it.sortedByDescending {
                        it.entryTime
                    }

//                    Log.d(TAG, "notes are : $it")
                }

        }
        viewModelScope.launch {
            checkForChanges()

        }
        notesRepository.listenToRemoteUpdateFromMainScreen()
    }

    private suspend fun checkForChanges() {
        val notesFromDb = notesRepository.getAllNotes()
        val notesFromFB = FirebaseFirestore.getInstance().collection("notes").get().await()
            .toObjects(Notes::class.java)

        Log.d(TAG, "checkForChanges: $notesFromFB    $notesFromDb")

        val localNotes = notesFromDb.firstOrNull()?.associateBy { it.id } ?: emptyMap()
        val remoteNotes = notesFromFB.associateBy { it.id }

        for (id in (localNotes.keys union remoteNotes.keys)) {
            val localNote = localNotes[id]
            val remoteNote = remoteNotes[id]

            Log.d(TAG, "checkForChanges: notesId $id")

            if (localNotes == null && remoteNote != null) {
                notesRepository.insertNotes(remoteNote)
            } else if (localNote != null && remoteNote == null) {
                notesRepository.saveNotesToFirebase(localNote)
            } else if (localNote != null && remoteNote != null) {
                if (localNote.entryTime > remoteNote.entryTime)
                    notesRepository.saveNotesToFirebase(localNote)
                else if (localNote.entryTime < remoteNote.entryTime)
                    notesRepository.insertNotes(remoteNote)
            }

        }


    }

    fun getAllNotes() = notesRepository.getAllNotes()

    fun getNotesFromDb() {

    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            notesRepository.deleteAllNotes()
        }
    }

}