package com.example.learnfirebase.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun EmailInput(
    modifier: Modifier,
    emailState: MutableState<String>,
    labelId: String = "Email",
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    onAction: KeyboardActions = KeyboardActions.Default
) {
    InputField(
        modifier,
        emailState,
        labelId,
        enabled,
        keyboardType = KeyboardType.Email,
        imeAction = imeAction,
        onAction = onAction
    )

}

@Composable
fun InputField(
    modifier: Modifier,
    valueState: MutableState<String>,
    labelId: String,
    enabled: Boolean,
    isSingleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onAction: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = valueState.value,
        onValueChange = { valueState.value = it },
        label = { Text(text = labelId) },
        singleLine = isSingleLine,
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .padding(5.dp)
            .fillMaxWidth(),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = onAction
    )
}

@Composable
fun ReaderLogo() {
    Text(
        text = "Reader Login Screen",
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.headlineLarge,
        fontSize = 20.sp
    )
}


@Composable
fun PasswordInput(
    modifier: Modifier,
    passwordState: MutableState<String>,
    labelId: String,
    enabled: Boolean,
    passwordVisibility: MutableState<Boolean>,
    imeAction: ImeAction = ImeAction.Done,
    onAction: KeyboardActions = KeyboardActions.Default
) {
    val visualTransformation = if (passwordVisibility.value) VisualTransformation.None
    else PasswordVisualTransformation()
    OutlinedTextField(
        value = passwordState.value,
        onValueChange = { passwordState.value = it },
        label = { Text(text = labelId) },
        textStyle = TextStyle(
            fontSize = 18.sp
        ),
        maxLines = 1,
        modifier = modifier
            .padding(5.dp)
            .fillMaxWidth(),
        enabled = enabled,
 keyboardOptions = KeyboardOptions(imeAction = imeAction, keyboardType = KeyboardType.Password),
        keyboardActions = onAction,
        visualTransformation = visualTransformation,
        trailingIcon = { PasswordVisibility(passwordVisibility) }
    )


}

@Composable
fun PasswordVisibility(passwordVisibility: MutableState<Boolean>) {
    val visible = passwordVisibility.value
    IconButton(onClick = { passwordVisibility.value = !visible },
        colors = IconButtonDefaults.iconButtonColors().copy(contentColor = Color.Red,
            containerColor = Color.LightGray)) {
        Icons.Default.Close
    }
}


@Composable
fun ShowButton(textid: String, loading: Boolean, validInputs: Boolean, onCLick: () -> Unit = {}) {
    Button(
        onClick = onCLick,
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth(),
        enabled = !loading && validInputs,
        shape = CircleShape
    ) {
        if (loading) CircularProgressIndicator()
        else Text(text = textid, modifier = Modifier.padding(5.dp))

    }
}

