package com.example.musicfilemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicfilemanager.ui.theme.*
import com.example.musicfilemanager.viewmodel.MusicViewModel
import java.util.Calendar

data class OldMusicItem(
    val id: String,
    val apiId: Int,
    val title: String,
    val artist: String,
    val year: Int,
    val age: Int // Số năm tuổi
)

@Preview
@Composable
fun OldMusicScreen(
    musicViewModel: MusicViewModel = viewModel(),
    onBack: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    onBottomItemClick: (String) -> Unit = {}
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    // State để lưu danh sách nhạc từ API
    var oldMusicList by remember { mutableStateOf<List<OldMusicItem>>(emptyList()) }
    val isLoading by musicViewModel.isLoading.collectAsState()
    val error by musicViewModel.error.collectAsState()

    // Load dữ liệu từ API khi màn hình được tạo
    LaunchedEffect(Unit) {
        val musicFiles = musicViewModel.getForAge40Plus()
        oldMusicList = musicFiles.map { musicFile ->
            val year = musicFile.releaseYear ?: currentYear
            OldMusicItem(
                id = musicFile.fileCode,
                apiId = musicFile.apiId,
                title = musicFile.music.title,
                artist = musicFile.music.artist,
                year = year,
                age = currentYear - year
            )
        }
    }

    Scaffold(
        topBar = {
            @Composable
            fun TopBarWithStatusBarPadding() {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Surface(
                        color = Color(0xFF252C3B),
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Outlined.Menu, null, tint = TextPrimary)
                            }
                            Text(
                                "Nhạc Cũ (< 40 năm)",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(40.dp))
                        }
                    }
                }
            }
            TopBarWithStatusBarPadding()
        },
        containerColor = Gray900,
        bottomBar = {
            BottomNavBar(
                current = "oldmusic",
                onClick = onBottomItemClick
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Gray900)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Thông tin tổng quan
            Surface(
                color = Gray800,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.MusicNote,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Nhạc dành cho độ tuổi trên 40",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${oldMusicList.size} bài hát",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Hiển thị loading
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            }
            // Hiển thị error
            else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Có lỗi xảy ra",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            // Hiển thị danh sách nhạc cũ
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    items(oldMusicList, key = { it.id }) { music ->
                        OldMusicCard(
                            music = music,
                            onClick = { onItemClick(music.apiId.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OldMusicCard(
    music: OldMusicItem,
    onClick: () -> Unit = {}
) {
    Surface(
        color = Gray800,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon nhạc
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gray700),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.MusicNote, contentDescription = null, tint = TextSecondary)
            }

            Spacer(Modifier.width(12.dp))

            // Thông tin bài hát
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    music.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    music.artist,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Năm phát hành
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(TagBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            music.year.toString(),
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    // Số năm tuổi
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${music.age} năm",
                            color = AccentPurple,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun BottomNavBar(current: String, onClick: (String) -> Unit) {
    NavigationBar(containerColor = Gray800) {
        val items = listOf(
            "home" to Icons.Outlined.Home,
            "library" to Icons.Outlined.MenuBook,
            "genre" to Icons.Outlined.Category,
            "oldmusic" to Icons.Outlined.MusicNote
        )
        items.forEach { (id, icon) ->
            NavigationBarItem(
                selected = id == current,
                onClick = { onClick(id) },
                icon = {
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null)
                    }
                },
                label = {
                    Text(
                        when (id) {
                            "home" -> "Trang chủ"
                            "library" -> "Thư viện"
                            "genre" -> "Thể loại"
                            "oldmusic" -> "Nhạc cũ"
                            else -> "Khác"
                        }
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentPurple,
                    selectedTextColor = AccentPurple,
                    indicatorColor = Gray700,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}
