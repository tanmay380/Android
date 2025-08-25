package com.example.notessyncapp.repository

import com.example.notessyncapp.dao.NotesDao
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class NotesRepositoryFactory @Inject constructor(private val notesDao: NotesDao)
{
    fun createNotesRepository( noteId: String, firestore: FirebaseFirestore = FirebaseFirestore.getInstance()): NotesRepository {
        return NotesRepository(notesDao, firestore, noteId)
    }

}