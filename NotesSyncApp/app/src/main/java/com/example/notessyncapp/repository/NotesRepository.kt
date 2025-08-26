package com.example.notessyncapp.repository

import android.util.Log
import androidx.room.Upsert
import com.example.notessyncapp.dao.NotesDao
import com.example.notessyncapp.model.Notes
import com.example.notessyncapp.screens.main.TAG
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NotesRepository(
    private val notesDao: NotesDao,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    var noteId: String
) {

    var notesRef: DocumentReference? =
        if (noteId.isNotEmpty()) firestore.collection("notes")
            .document(noteId)
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

    fun listenToRemoteUpdateFromNotesAdd() {
        Log.d(TAG, "listenToRemoteUpdate: ${notesRef}  ")
        notesRef?.addSnapshotListener { snapshot, error ->


            Log.d(TAG, "listenToRemoteUpdateinside add: $error  ${snapshot?.data}")
            if (error != null) return@addSnapshotListener

            if (snapshot != null && snapshot.exists()) {
                val remoteNotes = snapshot.toObject(Notes::class.java)
                Log.d("tanmay", "new nores is $remoteNotes")
                Log.d(TAG, "listenToRemoteUpdateFromNotesAdd")
//                for ()
                if (remoteNotes != null) {

//                    Log.d(TAG, "listenToRemoteUpdate: ${snapshot.data?.get("entryTime").toString().split("(?<=Timestamp\\(seconds=)\\d+")[0]}")
                    CoroutineScope(Dispatchers.IO).launch {
                        val local = getNotesByID(noteId).firstOrNull()
                        Log.d(
                            TAG,
                            "listenToRemoteUpdate: ${remoteNotes.entryTime.after(local?.entryTime)}"
                        )
                        if (remoteNotes.entryTime.after(local?.entryTime))
                            insertOrUpdate(remoteNotes)
                    }
                }
            }
        }
    }

    fun listenToRemoteUpdateFromMainScreen() {
        val colectionRef = firestore.collection("notes")
        colectionRef.addSnapshotListener { r, e ->
//            Log.d(TAG, "listenToRemoteUpdateFromMainScreen: $r   $e")
            if (e != null) return@addSnapshotListener
            if (r != null) {
                for (dc in r.documentChanges) {
                    Log.d(TAG, "listenToRemoteUpdateFromMainScreen: dc type is ${dc.type}")
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d(TAG, "listenToRemoteUpdateFromMainScreen: ${dc.document.data}")
                            CoroutineScope(Dispatchers.IO).launch {
                                insertOrUpdate(dc.document.toObject(Notes::class.java))
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            Log.d(
                                TAG,
                                "listenToRemoteUpdateFromMainScreen: Modifies ${dc.document.data}"
                            )
                            CoroutineScope(Dispatchers.IO).launch {
                                insertOrUpdate(dc.document.toObject(Notes::class.java))
                            }
                        }

                        DocumentChange.Type.REMOVED -> TODO()
                    }
                }

            }
        }

    }

}