package com.example.musicfilemanager.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicfilemanager.model.Genre
import com.example.musicfilemanager.ui.theme.Gray800
import com.example.musicfilemanager.ui.theme.Gray900
import com.example.musicfilemanager.ui.theme.TextPrimary
import com.example.musicfilemanager.ui.theme.TextSecondary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMusicScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit = {}
) {
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf(Genre.Pop) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val pickMusic = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                fileUri = uri
                // gợi ý: đọc metadata thật ở đây (sau sẽ nối ContentResolver/MediaMetadataRetriever)
            }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm File Nhạc", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) }
                }
            )
        },
        containerColor = Gray900
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ảnh/khung chọn file
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Gray800)
                    .padding(24.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.AudioFile, null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        // chỉ cho chọn âm thanh
                        pickMusic.launch(arrayOf("audio/*", "application/octet-stream"))
                    }) {
                        Text(if (fileUri == null) "Chọn File Nhạc" else "Đã chọn file")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            @Composable
            fun field(
                label: String,
                value: String,
                onChange: (String) -> Unit,
                placeholder: String = ""
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onChange,
                    label = { Text(label) },
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )
            }

            field("Mã File", value = fileUri?.lastPathSegment ?: "", onChange = {}, placeholder = "Tự lấy từ URI")
            field("Tên Bài Hát", title, { title = it }, "VD: Bài hát mẫu")
            field("Nghệ Sĩ", artist, { artist = it }, "VD: Nghệ sĩ A")
            field("Album", album, { album = it }, "VD: Album Demo")
            field("Năm Phát Hành", year, { year = it }, "VD: 2020")
            field("Thời lượng", duration, { duration = it }, "VD: 4:00")
            field("Kích thước", size, { size = it }, "VD: 5.0 MB")

            // Dropdown thể loại
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    readOnly = true,
                    value = genre.name,
                    onValueChange = {},
                    label = { Text("Thể loại") },
                    trailingIcon = { Icon(Icons.Outlined.KeyboardArrowDown, null) }
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    Genre.values().forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g.name) },
                            onClick = { genre = g; expanded = false }
                        )
                    }
                }
            }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(20.dp))

            GradientButton(
                text = "Lưu",
                modifier = Modifier.fillMaxWidth()
            ) {
                // Validate đơn giản
                if (fileUri == null || title.isBlank()) {
                    error = "Vui lòng chọn file và nhập Tên Bài Hát."
                    return@GradientButton
                }
                error = null
                // TODO: lưu vào Room/Repository rồi:
                onSaved()
            }
        }
    }
}

/** Nút gradient kiểu neon */
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(
        listOf(Color(0xFF5AC8FA), Color(0xFFB06BF7))
    )
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(gradient, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}