package com.example.notessyncapp.repository

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.room.Upsert
import com.example.notessyncapp.dao.NotesDao
import com.example.notessyncapp.model.Notes
import com.example.notessyncapp.screens.main.TAG
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date

class NotesRepository(
    private val notesDao: NotesDao,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    var noteId: String
) {

    var notesRef: DocumentReference? =
        if (noteId.isNotEmpty()) firestore.collection("notes")
            .document("6f4ab34b-6078-417b-84c5-d3d521542ef7")
        else null

    fun getAllNotes() = notesDao.getAllNotes()

    fun getNotesByID(id: String): Flow<Notes?> = notesDao.getNoteById(id)

    suspend fun deleteNotesByID(id: Int) = notesDao.deleteNoteById(id)

    suspend fun deleteAllNotes() = notesDao.deleteAllNotes()

    suspend fun updateNotes(id: String, title: String, description: String, entryTime: Long) =
        notesDao.updateNote(id, title, description, entryTime)

    suspend fun insertNotes(notes: Notes) = notesDao.insertNote(notes)

    @Upsert
    suspend fun insertOrUpdate(notes: Notes) = notesDao.insertOrUpdate(notes)

    fun listenToRemoteUpdate() {
//        Log.d(TAG, "listenToRemoteUpdate: ${notesRef}  $noteId")
        notesRef?.addSnapshotListener { snapshot, error ->


            Log.d(TAG, "listenToRemoteUpdateinside add: $error  ${snapshot?.data}")
            if (error != null) return@addSnapshotListener

            if (snapshot != null && snapshot.exists()) {
                val remoteNotes = snapshot.toObject(Notes::class.java)
                Log.d("tanmay", "new nores is $remoteNotes")
                if (remoteNotes != null) {


//                    Log.d(TAG, "listenToRemoteUpdate: ${snapshot.data?.get("entryTime").toString().split("(?<=Timestamp\\(seconds=)\\d+")[0]}")
                    CoroutineScope(Dispatchers.IO).launch {
                        val local = getNotesByID(noteId).firstOrNull()
                        Log.d(TAG, "listenToRemoteUpdate: ${remoteNotes.entryTime.after(local?.entryTime)}")
//                        val localCurrentTimeStamp = Regex("(?<=Timestamp\\(seconds=)\\d+").find(snapshot.data?.get("entryTime").toString())?.value/*) split("(?<=Timestamp\\(seconds=)\\d+")[0]*/
//                        Log.d(TAG, "listenToRemoteUpdate: ${remoteNotes.entryTime.time}  ${localCurrentTimeStamp} ${Date(remoteNotes.entryTime.time)}   next  ${Date(localCurrentTimeStamp!!.toLong())}")
//                        if (remoteNotes.entryTime.time > localCurrentTimeStamp.toLong())
                            insertOrUpdate(remoteNotes)
                    }
                }
            }
        }
    }

}