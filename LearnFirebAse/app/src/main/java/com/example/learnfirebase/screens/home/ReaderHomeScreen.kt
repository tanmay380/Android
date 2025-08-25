package com.example.learnfirebase.screens.home

import android.widget.ImageButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.learnfirebase.navigation.ReaderScreens
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderHomeScreen(navController: NavHostController) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Row {
                    Text(text = "Reader")

                    Spacer(modifier = Modifier.width(40.dp))
                }
            },
            actions = {
                IconButton(onClick = {
                    FirebaseAuth.getInstance().signOut().run {
                        navController.navigate(ReaderScreens.LoginScreen.name)
                    }
                }) {
                    Icon(Icons.Filled.Logout, contentDescription = "")
                }
            }

        )
    }) {
        Text("i am home", modifier = Modifier.padding(it))
    }
}