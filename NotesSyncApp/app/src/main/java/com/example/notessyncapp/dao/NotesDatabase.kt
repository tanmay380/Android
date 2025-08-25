package com.example.notessyncapp.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.notessyncapp.model.Notes
import com.example.notessyncapp.utils.DateConverter
import com.example.notessyncapp.utils.UUIDConverter

@Database(entities = [Notes::class], version = 4, exportSchema = false)
@TypeConverters(DateConverter::class, UUIDConverter::class)
abstract class NotesDatabase : RoomDatabase(){
    abstract fun notesDao(): NotesDao
}