package com.example.jettrivia.repository

import android.util.Log
import com.example.jettrivia.data.DataOrException
import com.example.jettrivia.model.QuestionItem
import com.example.jettrivia.network.QuestionApi
import javax.inject.Inject

class QuestionRepository @Inject constructor(private val api: QuestionApi) {
    private val dataOrException = DataOrException<ArrayList<QuestionItem>, Boolean, Exception>()

    suspend fun getAllQuestion(): DataOrException<ArrayList<QuestionItem>, Boolean, Exception>{
        try {
            dataOrException.loading = true
            dataOrException.data = api.getAllQuestion()
            if (dataOrException.data.toString().isNotEmpty()) dataOrException.loading = false
        }
        catch (exception : Exception){
            dataOrException.e = exception
            Log.d("tanmay", "getAllQuestion: ${dataOrException.e!!.localizedMessage}" )
        }
        return dataOrException
    }
}