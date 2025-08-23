package com.example.notesapp.repository

import com.example.notesapp.data.NoteDatabaseDao
import com.example.notesapp.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class NoteRepository @Inject constructor(private val noteDatabaseDao: NoteDatabaseDao) {
    suspend fun addNote(notr: Note) = noteDatabaseDao.insert(notr)
    suspend fun updateNote(notr: Note) = noteDatabaseDao.update(notr)
    suspend fun deleteNote(notr: Note) = noteDatabaseDao.deleteNote(notr)
    suspend fun deleteALlNotes() = noteDatabaseDao.deleteAll()
    suspend fun getALlNotes():Flow<List<Note>> =
        noteDatabaseDao.getAllNotes()
            .flowOn(Dispatchers.IO)
            .conflate()

}