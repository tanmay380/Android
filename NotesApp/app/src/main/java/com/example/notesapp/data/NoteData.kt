package com.example.notesapp.data

import com.example.notesapp.model.Note

class NoteDataSource{
    fun loadNotes(): List<Note>{
        return listOf(
            Note(title = "Shopping", description = "Buy milk, eggs, and bread"),
            Note(title = "Workout",description = "Evening run at 7 PM"),
            Note(title = "Meeting", description ="Project sync with team"),
            Note(title = "Reading", description ="Finish chapter 4 of novel"),
            Note(title = "Groceries", description ="Pick up fruits and veggies"),
            Note(title = "Call Mom",description = "Check in and share updates"),
            Note(title = "Ideas", description ="Brainstorm app redesign features"),
            Note(title = "Bills", description ="Pay electricity and water dues"),
            Note(title = "Birthday",description = "Plan Sam’s birthday gift"),
            Note(title = "Travel",description = "Book tickets for weekend trip")
        )
    }
}