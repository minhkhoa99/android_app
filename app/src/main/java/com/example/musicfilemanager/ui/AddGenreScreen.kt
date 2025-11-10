package com.example.musicfilemanager.ui.genres

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicfilemanager.ui.theme.*

@Preview
@Composable
fun AddGenreScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Scaffold(
        topBar = { PillTopBar(title = "Thêm Thể Loại Mới", onBack = onBack) },
        containerColor = Gray900
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // Icon GENRE “neon” (giả lập)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0x3321D4FD), Color(0x333C1CCF), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF5AC8FA), Color(0xFFB06BF7))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("GENRE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Tạo hoặc cập nhật thông tin thể loại",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(18.dp))

            FilledInput(
                value = code,
                onValueChange = { code = it.uppercase() },
                label = "Mã Thể Loại",
                placeholder = "Nhập mã thể loại (VD: POP, ROCK)",
                leading = { Icon(Icons.Outlined.Tag, null, tint = TextSecondary) }
            )
            Spacer(Modifier.height(12.dp))
            FilledInput(
                value = name,
                onValueChange = { name = it },
                label = "Tên Thể Loại",
                placeholder = "Nhập tên thể loại (VD: Pop Music)",
                leading = { Icon(Icons.Outlined.MusicNote, null, tint = TextSecondary) }
            )
            Spacer(Modifier.height(12.dp))
            FilledInput(
                value = desc,
                onValueChange = { desc = it },
                label = "Mô Tả",
                placeholder = "Mô tả chi tiết về thể loại...",
                leading = { Icon(Icons.Outlined.Description, null, tint = TextSecondary) },
                singleLine = false,
                minLines = 3
            )

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = "Lưu Thể Loại",
                modifier = Modifier.fillMaxWidth()
            ) {
                // TODO: validate + lưu DB
                onSaved()
            }

            Spacer(Modifier.height(14.dp))
        }
    }
}

/* ------- UI helpers ------- */

@Composable
private fun PillTopBar(title: String, onBack: () -> Unit) {
    // Thanh tiêu đề “viên thuốc”
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
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = TextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(40.dp)) // giữ cân đối bên phải
        }
    }
}

@Composable
private fun FilledInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leading: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Text(label, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(6.dp))
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Gray800),
        placeholder = { Text(placeholder, color = TextSecondary) },
        leadingIcon = leading,
        singleLine = singleLine,
        minLines = minLines,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Gray800,
            unfocusedContainerColor = Gray800,
            disabledContainerColor = Gray800,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = TextPrimary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF5AC8FA), Color(0xFFB06BF7)))
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(gradient, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}
