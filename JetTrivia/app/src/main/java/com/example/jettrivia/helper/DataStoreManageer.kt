package com.example.jettrivia.helper

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.jettrivia.data.dataStore
import com.example.jettrivia.utils.Constants.QUESTION_INDEX
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreManageer @Inject constructor(@ApplicationContext private val context: Context) {


    suspend fun saveQuestionIndex(index: Int) {
        context.dataStore.edit {
            it[QUESTION_INDEX] = index
        }
    }

    val questionIndex: Flow<Int> = context.dataStore.data
        .map {
            it[QUESTION_INDEX] ?: 0

        }
}