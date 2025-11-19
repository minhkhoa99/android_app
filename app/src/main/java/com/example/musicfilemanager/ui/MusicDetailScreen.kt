package com.example.musicfilemanager.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicfilemanager.model.MusicDetail
import com.example.musicfilemanager.ui.theme.AccentPurple
import com.example.musicfilemanager.viewmodel.GenreViewModel
import com.example.musicfilemanager.viewmodel.MusicViewModel
import com.example.musicfilemanager.ui.theme.Gray900
import com.example.musicfilemanager.ui.theme.TextPrimary
import com.example.musicfilemanager.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MusicDetailScreen(
    musicId: String = "1",
    musicViewModel: MusicViewModel = viewModel(),
    genreViewModel: GenreViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    var musicDetail by remember { mutableStateOf<MusicDetail?>(null) }
    val isLoading by musicViewModel.isLoading.collectAsState()
    val error by musicViewModel.error.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    // Load music detail from API
    LaunchedEffect(musicId) {
        val id = musicId.toIntOrNull()
        if (id != null) {
            musicDetail = musicViewModel.loadMusicFileById(id)
        }
    }

    // Lấy genre name từ API qua GenreViewModel
    val genresWithId by genreViewModel.genresWithId.collectAsState()
    val genreName = remember(musicDetail?.genreId, genresWithId) {
        musicDetail?.genreId?.let { genreId ->
            genresWithId.find { it.apiId == genreId }?.name ?: "Unknown"
        } ?: "-"
    }

    Scaffold(
        containerColor = Gray900
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    // Loading indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
                error != null -> {
                    // Error message
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lỗi: $error", color = TextSecondary)
                            Spacer(Modifier.height(8.dp))
                            Text("Không thể tải thông tin file nhạc", color = TextSecondary)
                        }
                    }
                }
                musicDetail != null -> {
                    // Display music details
                    Column(
                        Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(16.dp)
                            .padding(top = 16.dp)
                    ) {
                        // Helper function for displaying rows
                        @Composable
                        fun RowItem(label: String, value: String) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = TextSecondary)
                                Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Format file size
                        val fileSize = musicDetail?.fileSize?.let { bytes ->
                            when {
                                bytes < 1024 -> "$bytes B"
                                bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                                bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                                else -> String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
                            }
                        } ?: "-"

                        // Format duration
                        val duration = musicDetail?.duration?.let { seconds ->
                            val minutes = seconds / 60
                            val secs = seconds % 60
                            String.format(Locale.US, "%d:%02d", minutes, secs)
                        } ?: "-"

                        RowItem("Tên File:", musicDetail?.fileName ?: "-")
                        RowItem("Nghệ sĩ:", musicDetail?.artist ?: "-")
                        RowItem("Album:", musicDetail?.album ?: "-")
                        RowItem("Năm phát hành:", musicDetail?.releaseYear?.toString() ?: "-")
                        RowItem("Thể loại:", genreName)
                        RowItem("Thời lượng:", duration)
                        RowItem("Kích thước:", fileSize)
                        
                        // Display download link with proper formatting
                        musicDetail?.downloadLink?.let { downloadLink ->
                            val fullDownloadLink = if (downloadLink.startsWith("http://") || downloadLink.startsWith("https://")) {
                                downloadLink
                            } else {
                                "http://localhost:3005$downloadLink"
                            }
                            
                            // Download link with smaller font and copy functionality
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Download Link:", color = TextSecondary)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        fullDownloadLink,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth(0.7f)
                                    )
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(fullDownloadLink))
                                        },
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text("Copy", fontSize = 10.sp)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Note: API doesn't return createdAt/updatedAt in current response
                        // Using current date as placeholder
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val currentDate = dateFormat.format(java.util.Date())
                        RowItem("Ngày tạo:", currentDate)
                        RowItem("Cập nhật:", currentDate)
                    }
                }
                else -> {
                    // No data
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Không tìm thấy thông tin file nhạc", color = TextSecondary)
                    }
                }
            }

            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }
    }
}
