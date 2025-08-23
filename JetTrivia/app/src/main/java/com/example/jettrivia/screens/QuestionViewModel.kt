package com.example.jettrivia.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jettrivia.data.DataOrException
import com.example.jettrivia.helper.DataStoreManageer
import com.example.jettrivia.model.QuestionItem
import com.example.jettrivia.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(private val repository: QuestionRepository,
    private val dataStoreManager: DataStoreManageer) : ViewModel(){
    val data: MutableState<DataOrException<ArrayList<QuestionItem>,
            Boolean,
            Exception>> = mutableStateOf(
        DataOrException(null, true, Exception(""))
    )
    val _questionIndex = MutableStateFlow(0)
    val questionIndex : StateFlow<Int> = _questionIndex

    init {
        getAllQuestion()
        viewModelScope.launch {
            dataStoreManager.questionIndex.collect {
                _questionIndex.value = it
            }
        }
        Log.d("tanmay", ":${questionIndex.value}    ${_questionIndex.value} ")
    }

    private fun getAllQuestion(){
        viewModelScope.launch {
            data.value.loading = true
            data.value = repository.getAllQuestion()
            if (data.value.data.toString().isNotEmpty()){
                Log.d("tanmay", "getAllQuestion: ${data.value.data?.size}")
                data.value.loading = false
            }
        }
    }

    fun saveQuestionIndex(index: Int){
        _questionIndex.value+=1
        viewModelScope.launch {
            dataStoreManager.saveQuestionIndex(_questionIndex.value)
        }
    }


}