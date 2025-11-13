package com.example.musicfilemanager.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicfilemanager.data.GenreRepository
import com.example.musicfilemanager.model.sampleMusics
import com.example.musicfilemanager.ui.theme.Gray900
import com.example.musicfilemanager.ui.theme.TextPrimary
import com.example.musicfilemanager.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MusicDetailScreen(
    musicId: String = "1",
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val music = remember(musicId) { sampleMusics.find { it.id == musicId } }

    // Lấy genre name từ repository
    val genres by GenreRepository.genres.collectAsState()
    val genreName = remember(music?.genreId, genres) {
        music?.genreId?.let { id ->
            genres.find { it.id == id }?.name ?: "Unknown"
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
            Column(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp)
                    .padding(top = 16.dp)
            ) {
                // Bảng thông tin 2 cột
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
                RowItem("Tên File:", music?.title ?: "-")
                RowItem("Nghệ sĩ:", music?.artist ?: "-")
                RowItem("Album:", music?.album ?: "-")
                RowItem("Năm phát hành:", "2020")
                RowItem("Thể loại:", genreName)
                RowItem("Thời lượng:", music?.duration ?: "-")
                RowItem("Kích thước:", "5.0 MB")
                RowItem("Ngày tạo:", "08/11/2025")
                RowItem("Cập nhật:", "08/11/2025")
            }
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }
    }
}
