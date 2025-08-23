package com.example.jettrivia.model

data class QuestionItem(val question: String = "",
                        val answer: String = "",
                        val category: String = "",
                        val choices: List<String>?= null)
