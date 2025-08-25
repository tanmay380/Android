package com.example.weathercomposeapp.widgets

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weathercomposeapp.model.Favorites
import com.example.weathercomposeapp.navigation.WeatherScreens
import com.example.weathercomposeapp.screens.favorites.FavoritesViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
//@Preview
@Composable
fun WeatherAppBar(
    title: String = "title",
    country: String = "",
    icon: ImageVector? = null,
    isMainScreen: Boolean = true,
    elevtion: Dp = 0.dp,
    navController: NavController,
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    onAddActionClicked: () -> Unit = {},
    onButtonClicked: () -> Unit = {}
) {
    val showDialog = remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    if (showDialog.value == true) {
        ShowSettingDropDownMenu(showDialog = showDialog, navController = navController)
    }

    val title = if (country.isEmpty()) title else "$title, $country"

    Log.d("tanmay", "WeatherAppBar: icons is null $icon")
    TopAppBar(
        modifier = Modifier
            .shadow(elevtion)
            .padding(4.dp)
            .fillMaxWidth(),
        title = {
            Text(
                text = title,
                color = Color.Black,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                modifier = Modifier.offset(x = 30.dp)
            )
        },
        actions = {
            if (isMainScreen) {
                IconButton(onClick = { onAddActionClicked.invoke() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon"
                    )
                }
                IconButton(onClick = {
                    showDialog.value = true
                }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Search icon"
                    )
                }
            } else Box {}
        },
        navigationIcon = {
            if (icon != null) {
                Icon(
                    imageVector = icon, contentDescription = "Back Icon",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable {
                        onButtonClicked.invoke()
                    })
            }
            if (isMainScreen) {
                val isALreadyfav = favoritesViewModel.favList.collectAsState().value.filter {
                    it.city == title
                }
                Icon(
                    imageVector = if (!isALreadyfav.isEmpty())
                        Icons.Default.Favorite
                    else
                        Icons.Default.FavoriteBorder, contentDescription = "fav Icon",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .scale(0.9f)
                        .clickable {
                            if (isALreadyfav.isEmpty()) {
                                favoritesViewModel.insertFavorite(
                                    Favorites(
                                        city = title,
                                        country = country
                                    )
                                )
                                Toast.makeText(context, "City Favorite", Toast.LENGTH_SHORT).show()
                            } else {
                                favoritesViewModel.deleteFavorite(
                                    Favorites(
                                        city = title,
                                        country = country
                                    )
                                )
                            }
                        })
            }
        }

    )
}

@Composable
fun ShowSettingDropDownMenu(showDialog: MutableState<Boolean>, navController: NavController) {
    var expanded by remember { mutableStateOf(true) }
    val items = listOf("About", "Favorites", "Settings")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd)
            .absolutePadding(top = 45.dp, right = 20.dp)
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                showDialog.value = false
            },
            modifier = Modifier
                .width(140.dp)
                .background(Color.White)

        ) {
            items.forEachIndexed { index, text ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        showDialog.value = false
                    },
                    text = {
                        Row(
                            modifier = Modifier
                                .padding(3.dp)
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        when (text) {
                                            "About" -> WeatherScreens.AboutScreen.name
                                            "Favorites" -> WeatherScreens.FavScreen.name
                                            else -> WeatherScreens.SettingsScreen.name
                                        }
                                    )
                                },
                        ) {
                            Icon(
                                imageVector = when (text) {
                                    "About" -> Icons.Default.Info
                                    "Favorites" -> Icons.Default.FavoriteBorder
                                    else -> Icons.Default.Settings
                                },
                                tint = Color.LightGray,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = text,

                                fontWeight = FontWeight.W400
                            )
                        }
                    }
                )
            }


        }
    }

}