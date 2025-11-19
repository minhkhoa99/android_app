package com.example.musicfilemanager.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.musicfilemanager.ui.theme.Gray800
import com.example.musicfilemanager.ui.theme.Gray900
import com.example.musicfilemanager.ui.theme.TextPrimary
import com.example.musicfilemanager.ui.theme.TextSecondary
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicfilemanager.viewmodel.GenreViewModel
import com.example.musicfilemanager.viewmodel.MusicViewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMusicScreen(
    musicId: String? = null, // null = thêm mới, có giá trị = chỉnh sửa
    onBack: () -> Unit,
    onSaved: () -> Unit = {}
) {
    // ViewModels
    val genreViewModel: GenreViewModel = viewModel()
    val musicViewModel: MusicViewModel = viewModel()

    val apiGenres by genreViewModel.genresWithId.collectAsState()
    val genresForMusic = remember(apiGenres) { apiGenres.filter { it.code != "all" } }

    // State từ MusicViewModel
    val isLoading by musicViewModel.isLoading.collectAsState()
    val apiError by musicViewModel.error.collectAsState()
    val successMessage by musicViewModel.successMessage.collectAsState()

    // Local error state
    var localError by remember { mutableStateOf<String?>(null) }

    // Upload state
    var uploadProgress by remember { mutableStateOf<String?>(null) }
    var uploadedFileCode by remember { mutableStateOf<String?>(null) }
    var uploadedDownloadLink by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Context và coroutine
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Khai báo các biến state trước
    var selectedGenreId by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }

    // State để lưu music detail từ API khi edit
    var musicDetail by remember { mutableStateOf<com.example.musicfilemanager.model.MusicDetail?>(null) }
    val isEditMode = musicId != null

    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2020") }
    var duration by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("5.0 MB") }
    var description by remember { mutableStateOf("") }
    var ageRange by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var ageRangeExpanded by remember { mutableStateOf(false) }

    // Load dữ liệu từ API khi ở chế độ edit
    LaunchedEffect(musicId) {
        if (musicId != null) {
            val apiId = musicId.toIntOrNull()
            if (apiId != null) {
                musicDetail = musicViewModel.loadMusicFileById(apiId)
            }
        }
    }

    // Populate form fields khi có musicDetail từ API
    LaunchedEffect(musicDetail, genresForMusic) {
        if (musicDetail != null) {
            title = musicDetail!!.fileName
            artist = musicDetail!!.artist ?: ""
            album = musicDetail!!.album ?: ""
            description = musicDetail!!.description ?: ""
            ageRange = musicDetail!!.ageRange ?: ""

            // Parse year từ releaseYear nếu có
            musicDetail!!.releaseYear?.let { year = it.toString() }

            // Parse duration từ seconds sang "MM:SS"
            musicDetail!!.duration?.let { seconds ->
                val minutes = seconds / 60
                val secs = seconds % 60
                duration = String.format("%d:%02d", minutes, secs)
            }

            // Parse size từ fileSize (bytes) sang "X.X MB"
            musicDetail!!.fileSize?.let { bytes ->
                val mb = bytes / (1024.0 * 1024.0)
                size = String.format("%.1f MB", mb)
            }

            // Set selectedGenreId từ genreId của musicDetail
            if (genresForMusic.isNotEmpty()) {
                val apiGenreId = musicDetail!!.genreId
                val matchingGenre = genresForMusic.find { it.apiId == apiGenreId }
                selectedGenreId = matchingGenre?.code ?: genresForMusic.first().code
            }
        }
    }

    // Danh sách độ tuổi
    val ageRangeOptions = listOf(
        "Mọi lứa tuổi",
        "Trẻ em",
        "Thanh thiếu niên",
        "18+",
        "40+",
        "Người cao tuổi"
    )

    // Hiển thị lỗi từ API
    LaunchedEffect(apiError) {
        if (apiError != null) {
            localError = apiError
        }
    }

    // Navigate back khi thành công
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(500)
            musicViewModel.clearSuccessMessage()
            musicViewModel.clearError()
            onSaved()
        }
    }

    // Cleanup khi component unmount
    DisposableEffect(Unit) {
        onDispose {
            musicViewModel.clearSelectedMusicDetail()
        }
    }

    // Clear error khi user chỉnh sửa
    LaunchedEffect(title, artist, album) {
        if (localError != null && apiError != null) {
            localError = null
            musicViewModel.clearError()
        }
    }

    // Khi danh sách genres tải xong nếu chưa chọn thì auto chọn genre đầu tiên
    LaunchedEffect(genresForMusic) {
        if (selectedGenreId.isBlank() && genresForMusic.isNotEmpty() && !isEditMode) {
            selectedGenreId = genresForMusic.first().code
        }
    }


    val pickMusic = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                fileUri = uri

                // Upload ngay sau khi chọn file
                coroutineScope.launch {
                    try {
                        isUploading = true
                        uploadProgress = "Đang chuẩn bị upload file..."
                        localError = null

                        // Convert URI sang File
                        val file = context.getFileFromUri(uri)
                        if (file == null) {
                            localError = "Không thể đọc file. Vui lòng chọn lại."
                            uploadProgress = null
                            isUploading = false
                            fileUri = null
                            return@launch
                        }

                        uploadProgress = "Đang upload file lên server..."

                        // Generate tempFileCode để gửi lên (server required)
                        val timestamp = System.currentTimeMillis()
                        val tempFileName = file.nameWithoutExtension.take(20)
                        val tempFileCode = "${tempFileName.replace(Regex("[^A-Za-z0-9]"), "_").uppercase()}_${timestamp}"

                        // Upload file - Gửi tempFileCode nhưng nhận fileCode chính thức từ server
                        val uploadResult = musicViewModel.uploadMusicFile(file, tempFileCode, file.name)

                        // Xóa temp file
                        file.delete()

                        if (uploadResult == null) {
                            localError = apiError ?: "Upload file thất bại. Vui lòng thử lại."
                            uploadProgress = null
                            isUploading = false
                            fileUri = null
                            uploadedFileCode = null
                            uploadedDownloadLink = null
                        } else {
                            // Lưu fileCode và downloadLink từ server
                            uploadedFileCode = uploadResult.fileCode
                            uploadedDownloadLink = uploadResult.downloadLink
                            uploadProgress = "✓ Upload thành công! File code: ${uploadResult.fileCode}"
                            isUploading = false

                            // Auto fill file name nếu title trống
                            if (title.isBlank()) {
                                title = file.nameWithoutExtension
                            }
                        }

                    } catch (e: Exception) {
                        localError = e.message ?: "Có lỗi xảy ra khi upload"
                        uploadProgress = null
                        isUploading = false
                        fileUri = null
                        uploadedFileCode = null
                        uploadedDownloadLink = null
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Chỉnh Sửa File Nhạc" else "Thêm File Nhạc",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
                .verticalScroll(rememberScrollState())
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
                        Text(
                            if (fileUri == null) {
                                if (isEditMode) "Chọn File Mới (tùy chọn)" else "Chọn File Nhạc"
                            } else {
                                "Đã chọn file"
                            }
                        )
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

            // Hiển thị tên file đã chọn (nếu có)
            if (fileUri != null) {
                OutlinedTextField(
                    value = fileUri?.lastPathSegment ?: "",
                    onValueChange = {},
                    label = { Text("File đã chọn") },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )
            }

            // Hiển thị fileCode từ server (sau khi upload thành công)
            if (uploadedFileCode != null) {
                OutlinedTextField(
                    value = uploadedFileCode ?: "",
                    onValueChange = {},
                    label = { Text("Mã File (từ Server)") },
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )
            }

            field("Tên Bài Hát", title, { title = it }, "VD: Bài hát mẫu")
            field("Nghệ Sĩ", artist, { artist = it }, "VD: Nghệ sĩ A")
            field("Album", album, { album = it }, "VD: Album Demo")
            field("Năm Phát Hành", year, { year = it }, "VD: 2020")
            field("Thời lượng", duration, { duration = it }, "VD: 4:00")
            field("Kích thước", size, { size = it }, "VD: 5.0 MB")
            field("Mô Tả", description, { description = it }, "VD: Bài hát hay")

            // Dropdown độ tuổi
            ExposedDropdownMenuBox(
                expanded = ageRangeExpanded,
                onExpandedChange = { ageRangeExpanded = !ageRangeExpanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    readOnly = true,
                    value = ageRange,
                    onValueChange = {},
                    label = { Text("Độ Tuổi (Tùy chọn)") },
                    placeholder = { Text("Chọn độ tuổi phù hợp") },
                    trailingIcon = { Icon(Icons.Outlined.KeyboardArrowDown, null) }
                )
                ExposedDropdownMenu(
                    expanded = ageRangeExpanded,
                    onDismissRequest = { ageRangeExpanded = false }
                ) {
                    ageRangeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                ageRange = option
                                ageRangeExpanded = false
                            }
                        )
                    }
                }
            }

            // Dropdown thể loại (lấy từ API)
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    readOnly = true,
                    value = genresForMusic.find { it.code == selectedGenreId }?.name
                        ?: if (genresForMusic.isNotEmpty()) genresForMusic.first().name else "",
                    onValueChange = {},
                    label = { Text("Thể loại") },
                    trailingIcon = { Icon(Icons.Outlined.KeyboardArrowDown, null) }
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    genresForMusic.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g.name) },
                            onClick = { selectedGenreId = g.code; expanded = false }
                        )
                    }
                }
            }

            // Hiển thị error message
            if (localError != null) {
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.Surface(
                    color = Color(0x20FF5252),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.AudioFile,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = localError ?: "",
                            color = Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Hiển thị success message
            if (successMessage != null) {
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.Surface(
                    color = Color(0x204CAF50),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.AudioFile,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = successMessage ?: "",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Hiển thị upload progress
            if (uploadProgress != null) {
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.Surface(
                    color = Color(0x205AC8FA),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF5AC8FA)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = uploadProgress ?: "",
                            color = Color(0xFF5AC8FA),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            GradientButton(
                text = if (isLoading) "Đang xử lý..." else if (isEditMode) "Cập Nhật" else "Lưu",
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isUploading
            ) {
                // Validate đơn giản
                if (isEditMode) {
                    // Ở chế độ chỉnh sửa, chỉ cần có title
                    if (title.isBlank()) {
                        localError = "Vui lòng nhập Tên Bài Hát."
                        return@GradientButton
                    }
                } else {
                    // Ở chế độ thêm mới, cần file đã upload và title
                    if (uploadedFileCode == null || uploadedDownloadLink == null || title.isBlank()) {
                        localError = "Vui lòng chọn file (đợi upload xong) và nhập Tên Bài Hát."
                        return@GradientButton
                    }
                }

                localError = null

                // Chuyển selectedGenreId (string) sang genreId (Int) từ API
                val genreWithId = genreViewModel.getGenreWithIdByCode(selectedGenreId)
                val genreApiId = genreWithId?.apiId

                if (genreApiId == null) {
                    localError = "Không tìm thấy thể loại. Vui lòng chọn lại."
                    return@GradientButton
                }

                // Parse duration từ "4:00" thành seconds
                val durationInSeconds = try {
                    val parts = duration.split(":")
                    if (parts.size == 2) {
                        val minutes = parts[0].toIntOrNull() ?: 0
                        val seconds = parts[1].toIntOrNull() ?: 0
                        minutes * 60 + seconds
                    } else {
                        240 // default 4 minutes
                    }
                } catch (_: Exception) {
                    240
                }

                // Parse file size từ "5.0 MB" thành bytes
                val fileSizeInBytes = try {
                    val sizeStr = size.replace(Regex("[^0-9.]"), "")
                    val sizeFloat = sizeStr.toFloatOrNull() ?: 5.0f
                    (sizeFloat * 1024 * 1024).toLong()
                } catch (_: Exception) {
                    5242880L // 5MB default
                }

                // Parse year
                val releaseYear = year.toIntOrNull()

                if (isEditMode) {
                    // Cập nhật file nhạc - Sử dụng musicDetail từ API
                    if (musicDetail == null) {
                        localError = "Không tìm thấy thông tin file nhạc để cập nhật"
                        return@GradientButton
                    }

                    val apiId = musicDetail!!.apiId

                    // Giữ nguyên fileCode cũ, không tạo mới
                    val fileCode = musicDetail!!.fileCode

                    // Nếu có upload file mới thì dùng downloadLink mới, không thì giữ nguyên
                    val downloadLink = if (uploadedDownloadLink != null) {
                        uploadedDownloadLink
                    } else {
                        musicDetail!!.downloadLink
                    }

                    val filePath = musicDetail!!.filePath ?: "/uploads/music/${fileCode.lowercase()}.mp3"
                    val fileType = musicDetail!!.fileType ?: "mp3"

                    musicViewModel.updateMusicFile(
                        id = apiId,
                        fileCode = fileCode,
                        fileName = title,
                        genreId = genreApiId,
                        filePath = filePath,
                        fileType = fileType,
                        downloadLink = downloadLink,
                        artist = artist.ifBlank { null },
                        album = album.ifBlank { null },
                        releaseYear = releaseYear,
                        description = description.ifBlank { null },
                        duration = durationInSeconds,
                        fileSize = fileSizeInBytes,
                        ageRange = ageRange.ifBlank { null }
                    )
                } else {
                    // Tạo mới file nhạc - SỬ DỤNG DATA ĐÃ UPLOAD
                    coroutineScope.launch {
                        try {
                            localError = null
                            uploadProgress = "Đang lưu thông tin vào database..."

                            // Sử dụng fileCode và downloadLink đã upload
                            val fileCode = uploadedFileCode!!
                            val downloadLink = uploadedDownloadLink!!

                            // Generate filePath và fileType
                            val filePath = "/uploads/music/${fileCode.lowercase()}.mp3"
                            val fileType = fileUri?.lastPathSegment?.substringAfterLast(".", "mp3") ?: "mp3"

                            // Tạo metadata với downloadLink đã có
                            val createSuccess = musicViewModel.createMusicFileAndWait(
                                fileCode = fileCode,
                                fileName = title,
                                genreId = genreApiId,
                                filePath = filePath,
                                fileType = fileType,
                                downloadLink = downloadLink,
                                artist = artist.ifBlank { null },
                                album = album.ifBlank { null },
                                releaseYear = releaseYear,
                                description = description.ifBlank { null },
                                duration = durationInSeconds,
                                fileSize = fileSizeInBytes,
                                ageRange = ageRange.ifBlank { null }
                            )

                            uploadProgress = null

                            if (!createSuccess) {
                                localError = apiError ?: "Lưu thông tin thất bại. Vui lòng thử lại."
                            } else {
                                // Reset uploaded data sau khi thành công
                                uploadedFileCode = null
                                uploadedDownloadLink = null
                            }

                        } catch (e: Exception) {
                            localError = e.message ?: "Có lỗi xảy ra khi lưu"
                            uploadProgress = null
                        }
                    }
                }
            }
        }
    }
}

/** Nút gradient kiểu neon */
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(
        listOf(Color(0xFF5AC8FA), Color(0xFFB06BF7))
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    if (enabled) gradient else Brush.horizontalGradient(listOf(Color(0xFF555555), Color(0xFF555555))),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = if (enabled) TextPrimary else Color(0xFF999999))
        }
    }
}

/**
 * Helper function để convert URI sang File
 */
private fun android.content.Context.getFileFromUri(uri: Uri): java.io.File? {
    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        // Lấy tên file thực từ ContentResolver
        var fileName = "temp_${System.currentTimeMillis()}.mp3"

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex) ?: fileName
            }
        }

        // Nếu vẫn không có extension, thêm .mp3
        if (!fileName.contains(".")) {
            fileName = "$fileName.mp3"
        }

        val tempFile = java.io.File(cacheDir, fileName)

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        tempFile
    } catch (e: Exception) {
        null
    }
}

