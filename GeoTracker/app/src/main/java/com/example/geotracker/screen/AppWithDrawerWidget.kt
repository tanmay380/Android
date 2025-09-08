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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.geotracker.components.SessionSummary
import com.example.geotracker.utils.Utils.formatDuration
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
    val config = LocalConfiguration.current

    val screenDp = config.screenWidthDp
    // Compute width as fraction of screen with sensible caps
    val drawerWidth = remember(screenDp) {
        when {
            screenDp < 600 -> { // phones
                // 80% of screen but not more than 320 dp and not less than 280 dp
                (screenDp.dp * 0.8f).coerceIn(280.dp, 320.dp)
            }

            screenDp < 840 -> { // large phones / small tablets
                // 45% of screen, capped between 320..400 dp
                (screenDp.dp * 0.45f).coerceIn(320.dp, 400.dp)
            }

            else -> { // tablets / desktop
                // 35% of screen, max 480 dp
                minOf(screenDp.dp * 0.35f, 480.dp)
            }
        }
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.currentValue == DrawerValue.Open,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Text(
                    "My Routes",
                    style = typography.titleLarge
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
                                viewModel = viewModel
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
        }
    }

}

// SessionRow.kt — replace your SessionRow with this nicer layout
@Composable
private fun SessionRow(
    session: SessionSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
    viewModel: TrackingViewModel
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
        Column {
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
            if (isSelected) {

                Row {
                    IconButton(
                        modifier = Modifier.wrapContentWidth(),
                        onClick = { viewModel.deleteSession(session.sessionId) }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

        }
    }
}

