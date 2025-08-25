package com.example.learnfirebase.screens.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learnfirebase.models.MUser
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class LoginScreenViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading


    fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                viewModelScope.launch {

                    if (it.isSuccessful) {
                        Log.d("tanmay", "signInWithEmailAndPassword: ${it.result.toString()}")
                        home()
                    } else {
                        Log.d("tanmay", "signInWithEmailAndPassword: ${it.result.toString()}")

                        try {

                        } catch (ex: Exception) {
                            Log.d("tanmay", "signInWithEmailAndPassword: $ex")
                        }
                    }
                }
            }
    }


    fun createUserWithEmailAndPassword(email: String, password: String, home: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                viewModelScope.launch {

                    if (it.isSuccessful) {
                        val displayName = it.result.user?.email?.split('@')?.get(0)
                        createuser(displayName)
                        home()
                    } else {
                        Log.d("tanmay", "createInWithEmailAndPassword: ${it.result.toString()}")

                        try {

                        } catch (ex: Exception) {
                            Log.d("tanmay", "signInWithEmailAndPassword: $ex")
                        }
                    }
                }
            }
            .addOnFailureListener {
//                    Toast.makeText()
            }

    }

    private fun createuser(displayName: String?) {
        val userId = auth.currentUser?.uid

        val user = MUser(
            id = null,
            userId = userId.toString(),
            displayName = displayName.toString(),
            avatarUrl = "",
            quote = "Life is great",
            profession = "Android Developer"
        )

        FirebaseFirestore.getInstance().collection("users").add(user)
    }
}
