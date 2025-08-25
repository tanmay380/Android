package com.example.notessyncapp.screens.addNote

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notessyncapp.model.Notes
import com.example.notessyncapp.repository.NotesRepository
import com.example.notessyncapp.repository.NotesRepositoryFactory
import com.example.notessyncapp.screens.main.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Added annotation
class AddNoteViewModel @Inject constructor(
    private val notesRepositoryfactory: NotesRepositoryFactory,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val _titleState = MutableStateFlow("")
    val title = _titleState.asStateFlow()

    val _noteId = MutableStateFlow<String?>(null)
    val noteId = _noteId.asStateFlow()

    var noteState: StateFlow<Notes?>? = null
    val db = FirebaseFirestore.getInstance()

    val _decriptionState = MutableStateFlow("")
    val description = _decriptionState.asStateFlow()

    val _updateNotes = MutableStateFlow<Notes?>(null)
    val updateNotes = _updateNotes.asStateFlow()

    private var notesRepository: NotesRepository

    private val _isUpdating =
        MutableStateFlow(false)

    val isUpdating = _isUpdating.asStateFlow()


    fun setTitle(newTitle: String) {
        _titleState.value = newTitle
    }

    fun setDescription(newTitle: String) {
        _decriptionState.value = newTitle
    }

    init {
        _noteId.value = savedStateHandle.get<String>("hello")
        notesRepository = notesRepositoryfactory.createNotesRepository(noteId = _noteId.value ?: "")
//        noteState = noteId.let {
//            notesRepository.getNotesByID(it.toString())
//                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(50000), null)
//        }
        Log.d("tanmay", "notes: $notesRepository")
        if (noteId.value != null) {
            Log.d("tanmay", "emtoy: estring not ${noteId.value}")
            viewModelScope.launch {
                notesRepository.getNotesByID(noteId.value!!).collect {
                    _updateNotes.value = it
                    _titleState.value = it?.title ?: ""
                    _decriptionState.value = it?.description ?: ""
                    _isUpdating.value = true
                }
            }
            Log.d(TAG, "update stare: ${_updateNotes.value}")
            notesRepository.notesRef = db.collection("notes").document(noteId.value!!)

            Log.d("tanmay", "value of init nore id: ${noteId.value} ${notesRepository.noteId}")
            notesRepository.listenToRemoteUpdate()

//                _noteId.value?.let {
//                    viewModelScope.launch {
//                        notesRepository.getNotesByID(noteId.value!!).collect { note ->
//                            note?.let {
//                                Log.d("tanmay", "hello: hrhrfvfcv")
//                                _titleState.value = it.title
//                                _decriptionState.value = it.description
//                            }
//                        }
//                    }
//                }
        }

    }

    fun saveNotes(notes: Notes) = viewModelScope.launch {
        Log.d("tanmay", "saveNotes: Notes should be saved")
        val id = notesRepository.insertNotes(notes)
        _updateNotes.value = notes
        _isUpdating.value = true
        _noteId.value = notes.id
        notesRepository.noteId = _noteId.value.toString()
        Log.d("tanmay", "saveNotes: $id")
        saveNotesToFirebase(notes)

    }

    fun getNotesByID(id: String): Flow<Notes?> {
        return notesRepository.getNotesByID(id)
    }

    fun updateNotes(notes: Notes) = viewModelScope.launch {
        Log.d("tanmay", "updateNotes: $notes")
        notesRepository.updateNotes(
            notes.id.toString()!!, notes.title, notes.description,
            notes.entryTime.time
        )
        updateNotestoFirebase(notes)
    }

    private fun updateNotestoFirebase(notes: Notes) {

        db.collection("notes")
            .document(notes.id)
            .set(notes).addOnSuccessListener {
                Log.d("tanmay", "updateNotestoFirebase: update the notes, pleaes check")
            }

    }

    fun saveNotesToFirebase(notes: Notes) {
        FirebaseAuth.getInstance().currentUser!!.uid.toString()

        val dbCollection = db.collection("notes").document(notes.id.toString())

//
        dbCollection.set(notes)
            .addOnSuccessListener { documentReference ->
                Log.d("tanmay", "saveNotesToFirebase: ${documentReference}  ")

                notesRepository.notesRef = db.collection("notes").document(notes.id)
                notesRepository.listenToRemoteUpdate()
//                dbCollection.document(docId)
//                    .update(hashMapOf("id" to docId) as Map<String, Any>)
//                    .addOnCompleteListener {
//                        if (it.isSuccessful){
//                            Log.d("tanmay", "saveNotesToFirebase: Successfully updated")
//                        }
//                    }

            }
    }

    fun onNoteChnaged(
        noteId: String,
        content: String
    ) {
//        val updateNotes = Notes(
//            id = noteId,
//            description = content
//        )
    }

    private var saveJob: Job? = null
    fun autoSave(
        noteId: String?,
        title: String,
        description: String
    ) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(500)
            if (noteId == null) {
                val newNote = Notes(title = title, description = description)
                saveNotes(newNote)
            } else {
                val updateNote = Notes(id = noteId, title = title, description = description)
                updateNotes(updateNote)
            }
            Log.d("tanmay", "autoSave: working $noteId")
        }

    }

}