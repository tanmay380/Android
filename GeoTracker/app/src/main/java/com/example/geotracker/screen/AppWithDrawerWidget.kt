package com.example.geotracker.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.geotracker.components.SessionSummary
import com.example.geotracker.utils.Utils.formatDuration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppWithDrawer(
    viewModel: TrackingViewModel,
    content: @Composable (drawerState: DrawerState, selectedId: Set<Long?>) -> Unit
) {
    val summaries by viewModel.sessionSummary.collectAsState()
    Log.d("tanmay", "AppWithDrawer: $summaries")
    val selectedId by viewModel.selectedSessionId.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.currentValue == DrawerValue.Open,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.padding(16.dp)) {
                Text(
                    "My Routes",
                    style = typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                if (summaries.isEmpty()) {
                    Text(
                        "No routes yet",
                        style = typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(summaries) { s ->
                            SessionRow(
                                s,
                                onClick = {
                                    // toggle selection and open the session (if you want both)
                                    viewModel.toggleSessionSelection(s.sessionId)
                                    if (!selectedId.contains(s.sessionId)) {
                                        viewModel.openSession(s.sessionId)
                                    } else
                                        viewModel.clearRouteBySessionId(s.sessionId)
                                },
                                isSelected = selectedId.contains(s.sessionId),
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content(drawerState, selectedId)

//            if (drawerState.currentValue == DrawerValue.Open) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .zIndex(1f)
//                        .background(Color.Black.copy(alpha = 0.32f)) // scrim
//                        .clickable(
//                            indication = null,
//                            interactionSource = remember { MutableInteractionSource() }
//                        ) {
//                            scope.launch { drawerState.close() }
//                        }
//                )
//            }
        }
    }

}

// SessionRow.kt — replace your SessionRow with this nicer layout
@Composable
private fun SessionRow(
    session: SessionSummary,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val startStr = dateFormat.format(Date(session.startTimeMs))
    val duration = formatDuration(session.durationMs)
    val distance = String.format(Locale.getDefault(), "%.2f km", session.distanceKm)

    Surface(
        color = if (isSelected)
            colorScheme.primary.copy(alpha = 0.12f)
        else
            colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column() {
                Text(
                    text = startStr,
                    style = typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$distance • $duration",
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // trailing distance / chevron
            Column(
                modifier = Modifier.padding(start = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = distance,
                    style = typography.bodyMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

//@Composable
//fun SessionRow(session: SessionSummary, isSelected: Boolean, onClick: () -> Unit) {
//    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()) }
//    val start = Date(session.startTimeMs)
//    val startStr = dateFormat.format(start)
//    val duration = formatDuration(session.durationMs)
//    val distance = String.format(Locale.getDefault(), "%.2f km", session.distanceKm)
//
//    // highlight color (subtle)
//    val background = if (isSelected)
//        Color.Green
//    else
//        Color.Red
//
//    Surface(
//        color = background,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 8.dp)
//            .clickable { onClick() } // toggle happens in caller
//    ) {
//        ListItem(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 8.dp),
//            headlineContent = {
//                Text(text = "$startStr", fontSize = 20.sp)
//            },
//            supportingContent = {
//                Text(text = "$duration")
//            },
//            trailingContent = {
//                Text(text = distance, fontSize = 13.sp)
//            }
//        )
//    }
//
//}