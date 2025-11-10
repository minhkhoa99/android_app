package com.example.musicfilemanager.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.musicfilemanager.ui.theme.*

/* -------------------- Public API -------------------- */

@Composable
fun StatsScreen(
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = { PillTopBar(title = "Báo cáo Thống kê", onBack = onBack) },
        containerColor = Gray900
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                StorageCard(
                    percent = 0.85f,
                    totalFiles = 150,
                    totalSize = "1.5 GB"
                )
            }
            item {
                GenreDonutCard(
                    data = listOf(
                        "Pop" to 0.45f,
                        "Rock" to 0.30f,
                        "Jazz" to 0.15f,
                        "Khác" to 0.10f
                    )
                )
            }
            item {
                YearBarChartCard(
                    years = listOf(2023, 2022, 2021, 2020, 2019, 2018).reversed(),
                    values = listOf(45, 45, 36, 32, 20, 28).reversed()
                )
            }
            item {
                OldSongsCard(
                    items = listOf(
                        OldSong("Old Song.m33", 1980, 65),
                        OldSong("Vintage Music.m33", 1975, 50)
                    )
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/* -------------------- Sections -------------------- */

@Composable
private fun StorageCard(percent: Float, totalFiles: Int, totalSize: String) {
    SectionCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dung lượng Lưu trữ", fontWeight = FontWeight.SemiBold)
            Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = TextSecondary)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NeonRing(
                progress = percent,
                size = 120.dp,
                stroke = 14.dp,
                centerText = "${(percent * 100).toInt()}%"
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Tổng Files: $totalFiles", color = TextSecondary)
                Text("Tổng dung lượng: $totalSize", color = TextSecondary)
                Text("Đã sử dụng: ${(percent * 100).toInt()}%", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun GenreDonutCard(data: List<Pair<String, Float>>) {
    SectionCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nhạc theo Thể loại", fontWeight = FontWeight.SemiBold)
            Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = TextSecondary)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DonutChart(
                values = data.map { it.second },
                colors = neonPalette(),
                size = 140.dp,
                thickness = 20.dp,
                gapDegrees = 4f
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                data.zip(neonPalette()).forEach { (item, color) ->
                    Legend(color, "${item.first} (${(item.second * 100).toInt()}%)")
                }
            }
        }
    }
}

@Composable
private fun YearBarChartCard(years: List<Int>, values: List<Int>) {
    SectionCard {
        Text("Nhạc theo Năm Phát hành", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        BarChart(
            labels = years.map { it.toString().takeLast(2) },
            values = values,
            maxValue = (values.maxOrNull() ?: 1).coerceAtLeast(1),
            barWidth = 22.dp
        )
    }
}

data class OldSong(val name: String, val year: Int, val age: Int)

@Composable
private fun OldSongsCard(items: List<OldSong>) {
    SectionCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nhạc Cũ (> 40 năm)", fontWeight = FontWeight.SemiBold)
            Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = TextSecondary)
        }
        Spacer(Modifier.height(8.dp))
        items.forEach { s -> OldSongRow(s) }
    }
}

/* -------------------- Pieces -------------------- */

@Composable
private fun PillTopBar(title: String, onBack: () -> Unit) {
    Surface(
        color = Color(0xFF252C3B),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp)
            .height(44.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = TextPrimary)
            }
            Text(title, modifier = Modifier.weight(1f), color = TextPrimary,
                style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(40.dp))
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = Gray800,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(12.dp).clip(CircleShape).background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

/* -------------------- Charts -------------------- */

@Composable
private fun NeonRing(progress: Float, size: Dp, stroke: Dp, centerText: String) {
    val bg = Brush.sweepGradient(listOf(Color(0xFF273043), Color(0xFF273043)))
    val grad = Brush.sweepGradient(listOf(Color(0xFF5AC8FA), Color(0xFFB06BF7), Color(0xFF5AC8FA)))
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(Modifier.fillMaxSize()) {
            val s = size.minDimension
            val pad = stroke.toPx() / 2
            val arcSize = Size(s - stroke.toPx(), s - stroke.toPx())
            // background ring
            drawArc(
                brush = bg, startAngle = -90f, sweepAngle = 360f,
                useCenter = false, topLeft = Offset(pad, pad), size = arcSize,
                style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
            )
            // progress ring
            drawArc(
                brush = grad, startAngle = -90f, sweepAngle = 360f * progress,
                useCenter = false, topLeft = Offset(pad, pad), size = arcSize,
                style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(centerText, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DonutChart(
    values: List<Float>,
    colors: List<Color>,
    size: Dp,
    thickness: Dp,
    gapDegrees: Float = 0f
) {
    val total = values.sum().takeIf { it > 0f } ?: 1f
    val sweep = values.map { it / total * (360f - gapDegrees * values.size) }
    Canvas(Modifier.size(size)) {
        var start = -90f
        val rect = Size(size.minDimension, size.minDimension)
        val inset = thickness.toPx() / 2
        val arcSize = Size(rect.width - thickness.toPx(), rect.height - thickness.toPx())
        values.indices.forEach { i ->
            drawArc(
                color = colors[i % colors.size],
                startAngle = start,
                sweepAngle = sweep[i],
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)
            )
            start += sweep[i] + gapDegrees
        }
    }
}

@Composable
private fun BarChart(
    labels: List<String>,
    values: List<Int>,
    maxValue: Int,
    barWidth: Dp
) {
    val grad = Brush.verticalGradient(listOf(Color(0xFF5AC8FA), Color(0xFFB06BF7)))
    val barSpace = 16.dp
    val height = 140.dp

    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val count = values.size
                val bw = barWidth.toPx()
                val spacing = barSpace.toPx()
                val totalWidth = count * bw + (count - 1) * spacing
                var x = (w - totalWidth) / 2f

                values.forEach { v ->
                    val ratio = (v.toFloat() / maxValue).coerceIn(0f, 1f)
                    val barH = h * ratio
                    drawRoundRect(
                        brush = grad,
                        topLeft = Offset(x, h - barH),
                        size = Size(bw, barH),
                        cornerRadius = CornerRadius(18f, 18f)
                    )
                    x += bw + spacing
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        // labels
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach {
                Text(it, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/* -------------------- Rows -------------------- */

@Composable
private fun OldSongRow(s: OldSong) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Gray700),
            contentAlignment = Alignment.Center
        ) {
            Text("♪", color = TextPrimary)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(s.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text("${s.year} (${s.age} yrs)", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        Text("${s.year - s.age}", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

/* -------------------- Utils -------------------- */

private fun neonPalette() = listOf(
    Color(0xFF5AC8FA),
    Color(0xFFB06BF7),
    Color(0xFF60E0B8),
    Color(0xFFE6A85F)
)
