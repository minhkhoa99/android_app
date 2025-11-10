package com.example.musicfilemanager.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicfilemanager.model.sampleMusics
import com.example.musicfilemanager.ui.theme.AccentPurple
import com.example.musicfilemanager.ui.theme.Gray700
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
                    .padding(16.dp)
            ) {
                // Thumbnail + nút "Thumbnail"
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    Box(
                        Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Gray700),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.Album, null, tint = TextSecondary) }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 96.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CameraAlt, null, tint = TextSecondary)
                            Spacer(Modifier.width(6.dp))
                            Text("Thumbnail")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

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
                RowItem("Thể loại:", music?.genre?.name ?: "-")
                RowItem("Thời lượng:", music?.duration ?: "-")
                RowItem("Kích thước:", "5.0 MB")
                RowItem("Ngày tạo:", "08/11/2025")
                RowItem("Cập nhật:", "08/11/2025")

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Mô tả file nhạc") }
                )

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { /* upload */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    Icon(Icons.Outlined.Upload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tải lên")
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5BC0DE)),
                        border = BorderStroke(1.dp, Color(0xFF5BC0DE))
                    ) {
                        Icon(Icons.Outlined.Edit, null); Spacer(Modifier.width(6.dp)); Text("Chỉnh sửa")
                    }
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F))
                    ) {
                        Icon(Icons.Outlined.Delete, null); Spacer(Modifier.width(6.dp)); Text("Xóa")
                    }
                }
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
