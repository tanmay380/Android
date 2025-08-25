package com.example.notessyncapp.model

import android.icu.text.DateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.type.DateTime
import java.time.Instant
import java.util.Date
import java.util.UUID

@Entity(tableName = "notes_table",)
data class Notes(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    val title: String ="",
    val description: String = "",
    val entryTime: Date = Date.from(Instant.now())
)
{
    fun toMap(): MutableMap<String, Any>{
        return mutableMapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "entryTime" to entryTime
        )

    }
}
data class NotesFB(
    val id: String,
    val title: String,
    val description: String,
    val entryTime: Date = Date.from(Instant.now())
){
    fun toMap(): MutableMap<String, Any>{
        return mutableMapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "entryTime" to entryTime
        )

    }
}