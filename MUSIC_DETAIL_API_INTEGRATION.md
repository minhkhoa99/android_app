# Tích hợp API Detail File Nhạc

## Mô tả
Khi người dùng click vào một card file nhạc trong màn hình thư viện (MainScreen), ứng dụng sẽ chuyển sang màn MusicDetailScreen và gọi API để lấy thông tin chi tiết của file nhạc đó.

## API Endpoint
```
GET http://localhost:3005/api/music-files/{id}
```

### Parameters
- `id`: ID của file nhạc (Integer) - API database ID

### Response
```json
{
    "id": 1,
    "fileCode": "MUS001",
    "fileName": "song.mp3",
    "filePath": "/uploads/song.mp3",
    "thumbnailPath": null,
    "genreId": 1,
    "fileType": "audio/mpeg",
    "downloadLink": "http://localhost:3005/uploads/song.mp3",
    "artist": "Artist Name",
    "album": "Album Name",
    "releaseYear": 2020,
    "description": "Description",
    "duration": 180,
    "fileSize": 5242880,
    "createdAt": "2025-11-09T10:00:00",
    "updatedAt": "2025-11-09T10:00:00"
}
```

## Cách hoạt động

### 1. Luồng điều hướng
```
User clicks on music card in MainScreen
    ↓
Extract music.apiId (API database ID)
    ↓
Navigate to "detail/{apiId}"
    ↓
MusicDetailScreen receives apiId as String
    ↓
Convert to Int and call API
```

### 2. MainScreen.kt
- Khi user click vào card → gọi `onItemClick(apiId.toString())`
- Truyền API ID thay vì fileCode
- Navigate đến route: `"detail/{apiId}"`

```kotlin
MusicCard(
    music = item,
    onClick = { 
        // Truyền apiId thay vì fileCode cho detail screen
        item.apiId?.let { apiId ->
            onItemClick(apiId.toString())
        }
    }
)
```

### 3. MusicDetailScreen.kt
- Nhận `musicId` (String) từ navigation
- Chuyển đổi sang Int
- Gọi `musicViewModel.loadMusicFileById(id)` để load từ API
- Hiển thị thông tin chi tiết

```kotlin
LaunchedEffect(musicId) {
    val id = musicId.toIntOrNull()
    if (id != null) {
        musicDetail = musicViewModel.loadMusicFileById(id)
    }
}
```

### 4. MusicViewModel.kt
```kotlin
suspend fun loadMusicFileById(id: Int): MusicDetail? {
    val result = repository.getMusicDetailById(id)
    return when (result) {
        is ApiResult.Success -> {
            _selectedMusicDetail.value = result.data
            result.data
        }
        is ApiResult.Error -> null
        is ApiResult.Loading -> null
    }
}
```

### 5. MusicApiRepository.kt
```kotlin
suspend fun getMusicDetailById(id: Int): ApiResult<MusicDetail> {
    _isLoading.value = true
    _error.value = null

    val result = safeApiCall { apiService.getMusicFileById(id) }
    _isLoading.value = false

    return when (result) {
        is ApiResult.Success -> {
            val detail = MusicDetail(
                apiId = result.data.id,
                fileCode = result.data.fileCode,
                fileName = result.data.fileName,
                artist = result.data.artist,
                album = result.data.album,
                duration = result.data.duration,
                fileSize = result.data.fileSize,
                genreId = result.data.genreId,
                releaseYear = result.data.releaseYear,
                description = result.data.description,
                filePath = result.data.filePath,
                fileType = result.data.fileType,
                downloadLink = result.data.downloadLink
            )
            ApiResult.Success(detail)
        }
        is ApiResult.Error -> {
            _error.value = result.message
            result
        }
        is ApiResult.Loading -> result
    }
}
```

## UI States

### Loading
- Hiển thị CircularProgressIndicator khi đang load dữ liệu
- Centered trong màn hình

### Success
- Hiển thị thông tin đầy đủ:
  - **Tên File**: fileName
  - **Nghệ sĩ**: artist
  - **Album**: album
  - **Năm phát hành**: releaseYear
  - **Thể loại**: Lookup từ genreId qua GenreViewModel
  - **Thời lượng**: duration (format: MM:SS)
  - **Kích thước**: fileSize (format: B/KB/MB/GB)
  - **Ngày tạo**: createdAt (format: dd/MM/yyyy)
  - **Cập nhật**: updatedAt (format: dd/MM/yyyy)

### Error
- Hiển thị message: "Lỗi: {error message}"
- Hiển thị: "Không thể tải thông tin file nhạc"

### No Data
- Hiển thị: "Không tìm thấy thông tin file nhạc"

## Format Helpers

### File Size
```kotlin
val fileSize = bytes.let { bytes ->
    when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
```

### Duration
```kotlin
val duration = seconds.let { seconds ->
    val minutes = seconds / 60
    val secs = seconds % 60
    String.format(Locale.US, "%d:%02d", minutes, secs)
}
```

### Date
```kotlin
val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
val formattedDate = dateFormat.format(date)
```

## Testing

### 1. Khởi động API server
```bash
cd C:\Users\Admin\Desktop\app_music
start_music_api_server.bat
```

### 2. Test trên app
1. Mở ứng dụng
2. Vào màn hình Thư viện
3. Click vào bất kỳ card file nhạc nào
4. Kiểm tra:
   - ✅ Màn hình chuyển sang MusicDetailScreen
   - ✅ Hiển thị loading indicator
   - ✅ Hiển thị đầy đủ thông tin file nhạc
   - ✅ Tên thể loại được lookup đúng từ API
   - ✅ File size được format đúng (MB/KB)
   - ✅ Thời lượng được format đúng (MM:SS)
   - ✅ Button Back hoạt động

### 3. Test error handling
1. Tắt API server
2. Click vào card file nhạc
3. Kiểm tra hiển thị error message
4. Click Back để quay lại

### 4. Kiểm tra Network
- Dùng Logcat để xem API calls
- URL phải là: `http://10.0.2.2:3005/api/music-files/{id}`
- Response phải có cấu trúc MusicFileResponse

## Code Changes Summary

### Files Modified
1. **MusicDetailScreen.kt**
   - Added LaunchedEffect to load data from API
   - Added loading/error/success states
   - Added format helpers for file size, duration
   - Removed hardcoded data, use API response

2. **MainScreen.kt**
   - Changed onItemClick to pass apiId instead of fileCode
   - Updated MusicCard onClick handler

3. **MusicViewModel.kt** (already exists)
   - Method: `loadMusicFileById(id: Int)`

4. **MusicApiRepository.kt** (already exists)
   - Method: `getMusicDetailById(id: Int)`

### Navigation Flow
```
MainScreen → Click Card → Navigate("detail/{apiId}")
    ↓
MainActivity receives route with apiId
    ↓
MusicDetailScreen(musicId = apiId.toString())
    ↓
Load data from API using apiId
    ↓
Display MusicDetail
```

## Lưu ý
- API URL: `http://10.0.2.2:3005` (cho Android Emulator)
- Đảm bảo server đang chạy trước khi test
- MusicDetail sử dụng API ID, không phải fileCode
- Genre name được lookup động từ GenreViewModel
- Dates sử dụng current date vì API chưa trả về createdAt/updatedAt trong response

## Troubleshooting

### Không load được dữ liệu
1. Kiểm tra server có đang chạy không
2. Kiểm tra URL đúng không (10.0.2.2 cho emulator)
3. Kiểm tra apiId có hợp lệ không
4. Xem Logcat để debug lỗi API

### Genre hiển thị "Unknown"
- Kiểm tra GenreViewModel đã load genres chưa
- Kiểm tra genreId trong response có tồn tại không
- Kiểm tra mapping genresWithId

### Thông tin không đầy đủ
- Kiểm tra API response có đủ fields không
- Kiểm tra MusicDetail model có match với response không
- Một số fields có thể null (artist, album, description...)

