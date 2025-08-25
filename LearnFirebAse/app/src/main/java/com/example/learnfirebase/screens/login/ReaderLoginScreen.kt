package com.example.learnfirebase.screens.login

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.learnfirebase.components.EmailInput
import com.example.learnfirebase.components.PasswordInput
import com.example.learnfirebase.components.ReaderLogo
import com.example.learnfirebase.components.ShowButton
import com.example.learnfirebase.navigation.ReaderScreens

@Preview
@Composable
fun ReaderLoginScreen(navController: NavHostController) {
    val showLoginForm = rememberSaveable { mutableStateOf(true) }
    val viewModel: LoginScreenViewModel = viewModel()
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReaderLogo()
            if (showLoginForm.value)
                UserForm(){email, password ->
                    //Todo -> login fb account
                    viewModel.signInWithEmailAndPassword(email, password){
                        navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                    }
                }
            else{
                UserForm(loading = false, isCreateAccount = true){email, password ->
                    //Todo -> create fb account
                    viewModel.createUserWithEmailAndPassword(email, password){
                        navController.navigate(ReaderScreens.ReaderHomeScreen.name)
                    }
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            val text = if (showLoginForm.value) "Sign up" else "Login"
            Text(text = "New User?")
            Text(text,
                modifier = Modifier.clickable{
                    showLoginForm.value=!showLoginForm.value
                }.padding(start = 5.dp),
                color = Color.Blue
            )
        }

    }
}

@Preview
@Composable
fun UserForm(
    loading: Boolean = false,
    isCreateAccount: Boolean = false,
    onDone: (String, String) -> Unit = { email, password -> }
) {
    val email = rememberSaveable {
        mutableStateOf("")
    }
    val password = rememberSaveable {
        mutableStateOf("")
    }
    val passwordVisibility = rememberSaveable { mutableStateOf(false) }
    val passwordFocusRequest = remember { FocusRequester()}

    val keyboardController = LocalSoftwareKeyboardController.current
    val valid = remember(email.value, password.value) {
        email.value.trim().isNotEmpty() && password.value.trim().isNotEmpty()
    }
    val modifier = Modifier
        .height(250.dp)
        .verticalScroll(rememberScrollState())

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isCreateAccount) {
            Text("please use a valid email address and password")
        }
        EmailInput(
//            Modifier,
            Modifier.focusRequester(passwordFocusRequest),
            email,
            onAction = KeyboardActions {
                passwordFocusRequest.requestFocus()
            },
            enabled = !loading
            )
        PasswordInput(
            modifier = Modifier.focusRequester(passwordFocusRequest),
            password,
            labelId = "Password",
            enabled = true,
            passwordVisibility,
            onAction = KeyboardActions {
                if (!valid) return@KeyboardActions
                onDone(email.value.trim(), password.value.trim())
                keyboardController?.hide()
            }
        )
        ShowButton(
            textid = if (isCreateAccount) "Create Account" else "Login",
            loading = loading,
            validInputs = valid
        ){
            onDone(email.value.trim(), password.value.trim())

            keyboardController?.hide()
        }
    }


}
