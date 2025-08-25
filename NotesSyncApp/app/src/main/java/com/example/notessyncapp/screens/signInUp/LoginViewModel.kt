package com.example.notessyncapp.screens.signInUp

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Constants
private const val TAG = "LoginViewModel"
// For Google One Tap sign-in, use this type. For Google Play services sign-in,
// use "com.google.android.gms.auth.api.identity. zowelSignInCredential".
const val TYPE_GOOGLE_ID_TOKEN_CREDENTIAL = "com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL"


class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoginMode = MutableStateFlow(true) // True for Login, False for Sign Up
    val isLoginMode = _isLoginMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun toggleLoginMode() {
        _isLoginMode.value = !_isLoginMode.value
        _error.value = null // Clear errors when switching modes
    }

    fun handleEmailAuth() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _error.value = "Email and password cannot be empty."
            return
        }
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result: AuthResult = if (_isLoginMode.value) {
                    auth.signInWithEmailAndPassword(_email.value, _password.value).await()
                } else {
                    auth.createUserWithEmailAndPassword(_email.value, _password.value).await()
                }
                _loginSuccess.value = result.user != null
                if (result.user == null) {
                    _error.value = "Authentication failed."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred."
                Log.e(TAG, "Email auth error: ", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun prepareGoogleSignInRequest(): GetCredentialRequest {
        // Replace with your actual Web Client ID
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId("990633823805-oq93eag6n0o9hk3o4k7ak5j5u15drl5j.apps.googleusercontent.com")
            .setFilterByAuthorizedAccounts(false)
            .build()

        Log.d("tanmay", "prepareGoogleSignInRequest: $googleIdOption")

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    fun handleSignInCredential(credential: Credential) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                } else {
                    _error.value = "Unsupported credential type: ${credential.type}"
                    Log.w(TAG, "Credential is not of type Google ID or unsupported.")
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Google Sign-In failed: ${e.message}"
                Log.e(TAG, "Handle sign in credential error: ", e)
                _isLoading.value = false
            }
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            _loginSuccess.value = authResult.user != null
            if (authResult.user == null) {
                _error.value = "Firebase Google authentication failed."
            }
        } catch (e: Exception) {
            _error.value = "Firebase Google authentication error: ${e.message}"
            Log.e(TAG, "Firebase auth with Google error: ", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
