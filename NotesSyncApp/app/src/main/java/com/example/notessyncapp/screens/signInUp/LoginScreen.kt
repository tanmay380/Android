package com.example.notessyncapp.screens.signInUp

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private const val TAG = "tanmay"

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit // Callback to navigate on successful login
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoginMode by viewModel.isLoginMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()

    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)
    val scope = rememberCoroutineScope()

    LaunchedEffect(loginSuccess) {
        Log.d(TAG, "LoginScreen: $loginSuccess")
        if (loginSuccess) {
            Log.d(TAG, "LoginScreen: inside if")
            onLoginSuccess() // Navigate to the next screen
        }
    }

    // Removed the LaunchedEffect for error to avoid double Snackbar/Toast if handled by button too.
    // ViewModel's error state will be used directly for Text field error display.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLoginMode) "Login" else "Sign Up",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                viewModel.onEmailChange(it)
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null // Show error state on the field
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = error != null // Show error state on the field
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.handleEmailAuth() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            // Show progress indicator only if this specific action is loading
            // For simplicity, we use the general isLoading. For more granular control,
            // viewModel would need separate loading flags for email vs google.
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isLoginMode) "Login" else "Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            viewModel.toggleLoginMode()
            viewModel.clearError() // Clear error when toggling mode
        }) {
            Text(if (isLoginMode) "Need an account? Sign Up" else "Have an account? Login")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("or")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val googleSignInRequest = viewModel.prepareGoogleSignInRequest()
                        Log.d(TAG, "LoginScreen: $googleSignInRequest")
                        val result = credentialManager.getCredential(
                            context = context,
                            request = googleSignInRequest
                        )
                        Log.d(TAG, "LoginScreen: $result")
                        viewModel.handleSignInCredential(result.credential)
                    } catch (e: GetCredentialException) {
                        Log.e(TAG, "Google Sign-In GetCredentialException: ", e)
                        // Update ViewModel error state to show to user
                        viewModel.setError("Google Sign-In failed: ${e.message ?: "Unknown error"}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Google Sign-In Exception: ", e)
                        viewModel.setError("An unexpected error occurred during Google Sign-In.")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            if (isLoading) { // Use general isLoading flag for simplicity
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign in with Google")
            }
        }

        // General loading indicator at the bottom if any operation is in progress.
        // This might be redundant if buttons show their own loading state.
        // if (isLoading) {
        //     Spacer(modifier = Modifier.height(16.dp))
        //     CircularProgressIndicator()
        // }
    }
}

// Extension function in ViewModel to set error (if not already public or if you want specific logic)
fun LoginViewModel.setError(message: String) {
    // This is a conceptual placement. Actual implementation should be inside LoginViewModel class
    // if you want to expose a direct error setting method like this from the screen.
    // For now, LoginViewModel.handleSignInCredential and other methods handle their errors internally.
    // If a direct setError is needed, it should be: this._error.value = message
    Log.d(TAG, "Setting error from LoginScreen: $message") // Example, viewModel._error.value = message
}

