package com.example.musicfilemanager.ui.genres

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicfilemanager.ui.theme.*
import com.example.musicfilemanager.viewmodel.GenreViewModel

@Composable
fun AddGenreScreen(
    genreToEdit: GenreUi? = null,
    viewModel: GenreViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    var code by remember { mutableStateOf(genreToEdit?.id ?: "") }
    var name by remember { mutableStateOf(genreToEdit?.name ?: "") }
    var desc by remember { mutableStateOf(genreToEdit?.description ?: "") }
    var ageRange by remember { mutableStateOf(genreToEdit?.ageRange ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val apiError by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Navigate back on success
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(500)
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            PillTopBar(
                title = if (genreToEdit == null) "Thêm Thể Loại Mới" else "Chỉnh Sửa Thể Loại",
                onBack = onBack
            )
        },
        containerColor = Gray900
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Icon GENRE "neon" (giả lập)
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
                if (genreToEdit == null) "Tạo thể loại mới cho bộ sưu tập" else "Cập nhật thông tin thể loại",
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
                leading = { Icon(Icons.Outlined.Tag, null, tint = TextSecondary) },
                enabled = !isLoading
            )
            Spacer(Modifier.height(12.dp))
            FilledInput(
                value = name,
                onValueChange = { name = it },
                label = "Tên Thể Loại",
                placeholder = "Nhập tên thể loại (VD: Pop Music)",
                leading = { Icon(Icons.Outlined.MusicNote, null, tint = TextSecondary) },
                enabled = !isLoading
            )
            Spacer(Modifier.height(12.dp))
            FilledInput(
                value = desc,
                onValueChange = { desc = it },
                label = "Mô Tả",
                placeholder = "Mô tả chi tiết về thể loại...",
                leading = { Icon(Icons.Outlined.Description, null, tint = TextSecondary) },
                singleLine = false,
                minLines = 3,
                enabled = !isLoading
            )
            Spacer(Modifier.height(12.dp))
            FilledInput(
                value = ageRange,
                onValueChange = { ageRange = it },
                label = "Độ Tuổi",
                placeholder = "VD: 18+, All Ages, 13-17, etc.",
                leading = { Icon(Icons.Outlined.Person, null, tint = TextSecondary) },
                enabled = !isLoading
            )

            // Show error messages
            if (errorMessage != null || apiError != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: apiError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Show success message
            if (successMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = successMessage ?: "",
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = if (isLoading) "Đang xử lý..." else if (genreToEdit == null) "Lưu Thể Loại" else "Cập Nhật",
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                // Validate
                if (code.isBlank()) {
                    errorMessage = "Vui lòng nhập mã thể loại"
                    return@GradientButton
                }
                if (name.isBlank()) {
                    errorMessage = "Vui lòng nhập tên thể loại"
                    return@GradientButton
                }

                errorMessage = null

                // Call API via ViewModel
                if (genreToEdit == null) {
                    // Create new genre
                    viewModel.createGenre(
                        code = code,
                        name = name,
                        description = desc.ifBlank { null },
                        ageRange = ageRange.ifBlank { null }
                    )
                } else {
                    // Update existing genre - use apiId from genreToEdit
                    val apiId = genreToEdit.apiId
                    if (apiId == null) {
                        errorMessage = "Không tìm thấy ID thể loại để cập nhật"
                        return@GradientButton
                    }

                    viewModel.updateGenre(
                        id = apiId,
                        code = code,
                        name = name,
                        description = desc.ifBlank { null },
                        ageRange = ageRange.ifBlank { null }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
        }
    }
}

/* ------- UI helpers ------- */

@Composable
private fun PillTopBar(title: String, onBack: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Thanh tiêu đề "viên thuốc"
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
}

@Composable
private fun FilledInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leading: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true
) {
    Text(label, color = TextPrimary, style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(6.dp))
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF5AC8FA), Color(0xFFB06BF7)))
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    if (enabled) gradient else Brush.horizontalGradient(listOf(Gray700, Gray700)),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview
@Composable
private fun PreviewAddGenreScreen() {
    AddGenreScreen(
        genreToEdit = null,
        onBack = {},
        onSaved = {}
    )
}

@Preview
@Composable
private fun PreviewEditGenreScreen() {
    AddGenreScreen(
        genreToEdit = GenreUi(
            id = "ROCK",
            name = "Rock",
            description = "Nhạc rock bùng nổ",
            fileCount = 50
        ),
        onBack = {},
        onSaved = {}
    )
}

