package com.example.notessyncapp.di

import android.content.Context
import androidx.room.Room
import com.example.notessyncapp.dao.NotesDao
import com.example.notessyncapp.dao.NotesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule{

    @Provides
    @Singleton
    fun providesNotesDao(notesDatabase: NotesDatabase): NotesDao = notesDatabase.notesDao()

    @Provides
    @Singleton
    fun providesNotesDatabase(@ApplicationContext context: Context): NotesDatabase =
        Room.databaseBuilder(
                context,
                NotesDatabase::class.java,
                "notes_db"
            ).fallbackToDestructiveMigration(false)
            .build()
}