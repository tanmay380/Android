// GeoTrackerReportCompose.kt
// Jetpack Compose UI layout for the provided screenshot. UI only — no business logic.

@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetailsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {},
        floatingActionButton = { GeoFloatingActionButton() },
        containerColor = Color(0xFF333739)
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
//            GeoTopBar()
            // Date/time header
            GeoTopBar("11 Sept 2025 10:56:01 pm")

            // Summary metrics row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                MetricCard(title = "28.89 mi", subtitle = "TRACK LENGTH", modifier = Modifier.weight(1f))
                MetricCard(title = "67 mph", subtitle = "MAX SPEED", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duration / average row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                MetricCard(title = "00:56:56", subtitle = "TRACK DURATION", modifier = Modifier.weight(1f))
                MetricCard(title = "30 mph", subtitle = "AVERAGE SPEED", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speed chart area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B3F41))
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Text("SPEED CHART", color = Color(0xFFBFC7CC), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Placeholder for chart
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2E3133))
                    ) {
                        SimpleChart(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle row (Distance | Duration | Elevation)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                ToggleChip("DISTANCE")
                Spacer(modifier = Modifier.width(8.dp))
                ToggleChip("DURATION")
                Spacer(modifier = Modifier.width(8.dp))
                ToggleChip("ELEVATION")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Elevation metrics and chart
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("184 ft", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("ASCENT", color = Color(0xFFBFC7CC), fontSize = 12.sp)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("85 ft", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("DESCENT", color = Color(0xFFBFC7CC), fontSize = 12.sp, textAlign = TextAlign.End)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B3F41))
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Text("ELEVATION CHART", color = Color(0xFFBFC7CC), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2E3133))
                    ) {
                        SimpleChart(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Bottom small legend row
            Text("38 mph", color = Color(0xFF9DA6AA), modifier = Modifier.align(Alignment.Start))
        }
    }
}

@Composable
private fun GeoTopBar(date:String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(date, color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.End) {
            IconButton(onClick = { /* share icon - no logic */ }) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color(0xFFBFC7CC)
                )
            }
            IconButton(onClick = { /* download - no logic */ },) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Download",
                    tint = Color(0xFFBFC7CC)
                )
            }
        }
    }

}

@Composable
private fun MetricCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .width(170.dp)
            .height(70.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF333739))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
            Text(text = subtitle, color = Color(0xFFBFC7CC), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ToggleChip(text: String) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(text = text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color(0xFFBFC7CC))
    }
}

@Composable
private fun SimpleChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(6.dp)) {
        // simple decorative waveform to show chart placeholder
        val w = size.width
        val h = size.height
        val points = 40
        val step = w / (points - 1)
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h * 0.7f)
            for (i in 1 until points) {
                val x = i * step
                val y = h * (0.2f + 0.5f * kotlin.math.abs(kotlin.math.sin(i * 0.25f)))
                lineTo(x, y)
            }
        }
        drawPath(path = path, color = Color(0xFF67E6D7), style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun GeoFloatingActionButton() {
    FloatingActionButton(onClick = { /* no-op */ }, containerColor = Color(0xFFE64A19)) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            // red record-like button
            Spacer(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color.White))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2A2E2F)
@PreviewScreenSizes()
@Composable
fun GeoReportPreview() {
    DetailsScreen()
}
