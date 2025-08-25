package com.example.notessyncapp.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun InputText(text: String, label: String,
              onAction: KeyboardActions = KeyboardActions.Default,
              onImeAction: ImeAction = ImeAction.None,
              maxLines: Int? = null,
              onTextChange: (String) -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = { onTextChange(it) },
        label = { Text(text = label) },
        modifier = Modifier.padding(10.dp),
        keyboardActions = onAction,
        maxLines = maxLines ?: Int.MAX_VALUE,
        keyboardOptions = KeyboardOptions(imeAction = onImeAction)
    )
}