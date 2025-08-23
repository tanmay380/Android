package com.example.notesapp.screen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.model.Note
import com.example.notesapp.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val repository: NoteRepository) : ViewModel() {
    //    private var noteList = mutableStateListOf<Note>()
    private val _noteList = MutableStateFlow<List<Note>>(emptyList())
    val noteList = _noteList.asStateFlow()

    var _name = mutableStateOf("")
    var name = mutableStateOf("")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getALlNotes().distinctUntilChanged()
                .collect{
                    listOfNotes ->
//                    if (listOfNotes.isNullOrEmpty()) {
//                        Log.d(TAG, ":em,pty list ")
//                    }else{
                        _noteList.value = listOfNotes
//                    }
                }
        }
//        noteList.addAll(NoteDataSource().loadNotes())
    }

    fun addNote(note: Note) = viewModelScope.launch {
        repository.addNote(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun setTitle(n: String) {name.value = n}
}