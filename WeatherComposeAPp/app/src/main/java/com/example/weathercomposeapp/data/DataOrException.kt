package com.example.weathercomposeapp.data

class DataOrException<T, Boolean, E: Exception>(
    var data: T? = null,
    var boolean: kotlin.Boolean = true,
    var exception: E? = null
)