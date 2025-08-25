package com.example.notessyncapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.notessyncapp.model.Notes
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("Select * from notes_table")
    fun getAllNotes(): Flow<List<Notes>>

    @Query("Select * from notes_table where id = :id")
    fun getNoteById(id: String): Flow<Notes?>

    @Query("Delete from notes_table where id = :id")
    suspend fun deleteNoteById(id: Int)

    @Query("Delete from notes_table")
    suspend fun deleteAllNotes()

    @Query("Update notes_table Set title = :title, description = :description, entryTime = :entryTime where  id= :uuid")
    suspend fun updateNote(uuid: String, title: String, description: String, entryTime: Long)

    @Insert
    suspend fun insertNote(notes: Notes) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(notes: Notes)

}