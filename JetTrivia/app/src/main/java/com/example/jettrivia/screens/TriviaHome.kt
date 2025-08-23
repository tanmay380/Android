package com.example.jettrivia.screens

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.jettrivia.component.Questions

@Composable
fun TriviaHome(viewModel: QuestionViewModel = hiltViewModel<QuestionViewModel>()){
    Questions(viewModel)
}