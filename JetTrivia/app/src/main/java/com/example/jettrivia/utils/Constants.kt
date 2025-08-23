package com.example.jettrivia.utils

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

object Constants {
    const val BASE_URL = "https://raw.githubusercontent.com/itmmckernan/triviaJSON/refs/heads/master/"
    val QUESTION_INDEX = intPreferencesKey("question_index")
}