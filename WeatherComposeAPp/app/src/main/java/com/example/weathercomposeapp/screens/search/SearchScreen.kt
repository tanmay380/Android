package com.example.weathercomposeapp.screens.search

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weathercomposeapp.navigation.WeatherScreens
import com.example.weathercomposeapp.widgets.WeatherAppBar
import kotlinx.coroutines.delay

@Composable
fun SeachScreen(navController: NavController) {
    Scaffold(topBar = {
        WeatherAppBar(
            navController = navController,
            icon = Icons.Default.ArrowBack,
            title = "Search",
            isMainScreen = false
        ) {
            navController.navigateUp()
        }
    }) {
        Surface(modifier = Modifier.padding(it)) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchBar(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Log.d("tanmay", "SeachScreen: $it")
                    navController.navigate(
                        WeatherScreens.MainScreen.name + "/$it"
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {}
) {
    val searchQueryState = rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val valid = remember(searchQueryState.value) {
        searchQueryState.value.trim().isNotEmpty()
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {

        focusRequester.requestFocus()
    }

    Column {
        CommonTextField(
            valueState = searchQueryState,
            placeholder = "Search City",
            onAction = KeyboardActions {
                if (!valid) return@KeyboardActions
                onSearch(searchQueryState.value.trim())
                searchQueryState.value = ""
                keyboardController?.hide()
            },
            onKeyboardType = KeyboardType.Text,
            modifier = Modifier.focusRequester(focusRequester)
        )

    }
}

@Composable
fun CommonTextField(
    valueState: MutableState<String>,
    placeholder: String,
    onAction: KeyboardActions,
    onKeyboardType: KeyboardType,
    imeAction: ImeAction = ImeAction.Next,
    onActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valueState.value,
        onValueChange = { valueState.value = it },
        label = {
            Text(text = placeholder)
        },
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = onKeyboardType, imeAction = imeAction),
        keyboardActions = onAction,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Blue,
            cursorColor = Color.Black
        ),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    )
}