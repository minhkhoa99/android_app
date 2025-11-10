package com.example.musicfilemanager.ui.genres

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Piano
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicfilemanager.ui.theme.*

data class GenreUi(
    val id: String,
    val name: String,
    val description: String,
    val fileCount: Int,
    val icon: GenreIcon
)
enum class GenreIcon { Rock, Pop, Jazz, HipHop }

@Preview
@Composable
fun GenreListScreen(
    genres: List<GenreUi> = sampleGenres(),
    onBack: () -> Unit = {},
    onAdd: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onOpen: (String) -> Unit = {}   // click cả item
) {
    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF252C3B),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                    .height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.Menu, null, tint = TextPrimary) }
                    Text(
                        "Quản Lý Thể Loại",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(40.dp))
                }
            }
        },
        floatingActionButton = { NeonFab(text = "Thêm Mới", onClick = onAdd) },
        containerColor = Gray900
    ) { inner ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 12.dp,
                bottom = inner.calculateBottomPadding() + 100.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(genres, key = { it.id }) { g ->
                GenreCard(
                    g,
                    onClick = { onOpen(g.id) },
                    onEdit = { onEdit(g.id) },
                    onDelete = { onDelete(g.id) },
                    onQuickAdd = { onAdd() }
                )
            }
        }
    }
}

@Composable
private fun GenreCard(
    g: GenreUi,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickAdd: () -> Unit
) {
    Surface(
        color = Gray800,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon trái
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Gray700),
                contentAlignment = Alignment.Center
            ) {
                when (g.icon) {
                    GenreIcon.Rock -> Icon(Icons.Outlined.Piano, null, tint = TextPrimary)
                    GenreIcon.Pop -> Icon(Icons.Outlined.MusicNote, null, tint = TextPrimary)
                    GenreIcon.Jazz -> Icon(Icons.Outlined.Piano, null, tint = TextPrimary)
                    GenreIcon.HipHop -> Icon(Icons.Outlined.Mic, null, tint = TextPrimary)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(g.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    g.description,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "(${g.fileCount} Files)",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.width(12.dp))

            // Nút tròn nhỏ
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallRoundButton(icon = Icons.Outlined.Edit, onClick = onEdit)
                SmallRoundButton(icon = Icons.Outlined.Delete, onClick = onDelete)
                SmallRoundButton(icon = Icons.Outlined.Add, onClick = onQuickAdd)
            }
        }
    }
}

@Composable
private fun SmallRoundButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Gray700,
        tonalElevation = 1.dp
    ) {
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = TextPrimary)
        }
    }
}

@Composable
private fun NeonFab(text: String, onClick: () -> Unit) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF5AC8FA), Color(0xFFB06BF7)))
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .background(gradient, RoundedCornerShape(28.dp))
    ) { Text(text) }
}

/* ---------- sample data để thấy UI ---------- */
private fun sampleGenres() = listOf(
    GenreUi("rock", "ROCK", "Nhạc rock bùng nổ", 50, GenreIcon.Rock),
    GenreUi("pop", "POP", "Nhạc pop hiện đại", 120, GenreIcon.Pop),
    GenreUi("jazz", "JAZZ", "Nhạc Jazz ngẫu hứng", 30, GenreIcon.Jazz),
    GenreUi("hiphop", "HIP HOP", "Văn hóa Hip Hop", 75, GenreIcon.HipHop)
)
