package com.example.notessyncapp.dao

class DataOrException<T, Boolean, E: Exception> {
    var data: T? = null
    var loading = true
    var exception: E? = null
}