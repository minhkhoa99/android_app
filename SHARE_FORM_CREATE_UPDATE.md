# Chia Sẻ Form Create/Update cho Music File

## Tóm tắt
Đã cập nhật AddMusicScreen để hỗ trợ cả chế độ **Thêm mới** và **Chỉnh sửa** file nhạc, sử dụng chung một form.

## API Endpoints

### 1. Create Music File
- **POST** `/api/music-files`
- Body: MusicFileRequest
- Response: MusicFileResponse

### 2. Update Music File
- **PUT** `/api/music-files/{id}`
- Path param: `id` (Integer) - API database ID
- Body: MusicFileRequest
- Response: MusicFileResponse

### 3. Get Music File Detail
- **GET** `/api/music-files/{id}`
- Path param: `id` (Integer) - API database ID
- Response: MusicFileResponse

## Thay đổi đã thực hiện

### 1. Music.kt - Models
```kotlin
data class Music(
    val id: String,              // fileCode for UI display
    val apiId: Int? = null,      // API database ID for editing/deleting ✅ THÊM MỚI
    val apiGenreId: Int? = null, // API genre ID
    // ... các fields khác
)

data class MusicDetail(           // ✅ MODEL MỚI cho edit mode
    val apiId: Int,
    val fileCode: String,
    val fileName: String,
    val artist: String?,
    val album: String?,
    val duration: Int?,
    val fileSize: Long?,
    val genreId: Int,
    val releaseYear: Int?,
    val description: String?,
    val filePath: String?,
    val fileType: String?,
    val downloadLink: String?
)
```

### 2. MusicApiRepository.kt

#### a. Thêm getMusicDetailById()
```kotlin
suspend fun getMusicDetailById(id: Int): ApiResult<MusicDetail> {
    // Load full music info từ API
    // Convert MusicFileResponse → MusicDetail
}
```

#### b. Cập nhật updateMusicFile()
```kotlin
suspend fun updateMusicFile(
    id: Int,
    // ... các params
    downloadLink: String? = null,  // ✅ THÊM MỚI
    // ... 
): ApiResult<Music>
```

#### c. Cập nhật toMusic()
```kotlin
private fun MusicFileResponse.toMusic(): Music {
    return Music(
        id = this.fileCode,
        apiId = this.id,  // ✅ THÊM MỚI - Lưu database ID
        // ...
    )
}
```

### 3. MusicViewModel.kt

#### a. Thêm selectedMusicDetail state
```kotlin
private val _selectedMusicDetail = MutableStateFlow<MusicDetail?>(null)
val selectedMusicDetail: StateFlow<MusicDetail?> = _selectedMusicDetail.asStateFlow()
```

#### b. Thêm loadMusicFileById()
```kotlin
suspend fun loadMusicFileById(id: Int): MusicDetail? {
    // Load music detail for editing
}
```

#### c. Cập nhật updateMusicFile()
```kotlin
fun updateMusicFile(
    id: Int,
    // ...
    downloadLink: String? = null,  // ✅ THÊM MỚI
    // ...
)
```

### 4. GenreViewModel.kt

#### Thêm getGenreByApiId()
```kotlin
fun getGenreByApiId(apiGenreId: Int): Genre? {
    // Map API genre ID → UI Genre object
}
```

### 5. AddMusicScreen.kt

#### a. Load dữ liệu khi ở chế độ edit
```kotlin
val musicDetail by musicViewModel.selectedMusicDetail.collectAsState()

LaunchedEffect(musicId) {
    if (musicId != null) {
        val apiId = musicId.toIntOrNull()
        if (apiId != null) {
            val detail = musicViewModel.loadMusicFileById(apiId)
            // Populate form với dữ liệu từ API
            title = detail.fileName
            artist = detail.artist ?: ""
            album = detail.album ?: ""
            // Convert duration từ seconds → "MM:SS"
            // Convert fileSize từ bytes → "X.XX MB"
            // Map genreId
        }
    }
}
```

#### b. Cleanup khi unmount
```kotlin
DisposableEffect(Unit) {
    onDispose {
        musicViewModel.clearSelectedMusicDetail()
    }
}
```

#### c. Update logic khi submit
```kotlin
if (isEditMode) {
    val apiId = musicDetail?.apiId
    val finalFileCode = uploadedFileCode ?: musicDetail.fileCode
    val finalDownloadLink = uploadedDownloadLink ?: musicDetail.downloadLink
    
    // Sử dụng metadata từ upload nếu có, fallback sang form
    val finalArtist = uploadedArtist ?: artist.ifBlank { null }
    val finalAlbum = uploadedAlbum ?: album.ifBlank { null }
    val finalFileSize = uploadedFileSize ?: fileSizeInBytes
    val finalDuration = uploadedDuration ?: durationInSeconds
    
    musicViewModel.updateMusicFile(
        id = apiId,
        fileCode = finalFileCode,
        fileName = title,
        genreId = genreApiId,
        downloadLink = finalDownloadLink,
        // ... các fields khác
    )
}
```

### 6. MainScreen.kt

#### Truyền apiId khi navigate đến edit
```kotlin
onEditClick = { 
    item.apiId?.let { apiId -> 
        onEditClick(apiId.toString()) 
    }
}
```

### 7. MainActivity.kt - Navigation

```kotlin
// Route cho thêm mới
composable(Routes.AddMusic) {
    AddMusicScreen(
        musicId = null,  // null = create mode
        onBack = { nav.popBackStack() },
        onSaved = { nav.popBackStack() }
    )
}

// Route cho chỉnh sửa
composable(
    route = Routes.EditMusic,
    arguments = listOf(navArgument("id"){ defaultValue = "1" })
) { backStack ->
    val id = backStack.arguments?.getString("id") ?: "1"
    AddMusicScreen(
        musicId = id,  // apiId as String = edit mode
        onBack = { nav.popBackStack() },
        onSaved = { nav.popBackStack() }
    )
}
```

## Luồng hoạt động

### Create Mode (musicId = null)
```
1. User clicks "Thêm File Nhạc"
2. Form hiển thị rỗng
3. User chọn file → Upload ngay
4. Auto-fill: artist, album, duration, fileSize từ upload response
5. User nhập/chỉnh sửa thông tin
6. Submit → POST /api/music-files
7. Success → Navigate back
```

### Edit Mode (musicId = apiId)
```
1. User clicks "Chỉnh sửa" ở card
2. Navigate với apiId (e.g., edit_music/5)
3. Load MusicDetail từ GET /api/music-files/5
4. Auto-fill form với dữ liệu hiện có
5. User chỉnh sửa thông tin
6. (Optional) Chọn file mới → Upload → Auto-fill metadata mới
7. Submit → PUT /api/music-files/5
8. Success → Navigate back
```

## Ưu điểm

✅ **Shared Form** - Một form cho cả create và update, giảm code duplicate  
✅ **Auto-fill** - Tự động điền thông tin từ API khi edit  
✅ **Flexible** - Có thể upload file mới khi edit  
✅ **Metadata Priority** - Ưu tiên metadata từ file thực tế (upload response)  
✅ **Fallback** - Dùng giá trị form nếu không có metadata  
✅ **Clean Navigation** - Sử dụng API ID thay vì fileCode  

## So sánh Create vs Update

| Tính năng | Create Mode | Edit Mode |
|-----------|------------|-----------|
| musicId parameter | `null` | `apiId` (Int as String) |
| Load data | Không | Load từ API |
| Chọn file | **Bắt buộc** | Tùy chọn |
| Upload file | Luôn có | Chỉ khi chọn file mới |
| Auto-fill từ upload | ✅ | ✅ (nếu upload mới) |
| Giữ nguyên fileCode | N/A | ✅ (nếu không upload mới) |
| API call | POST /api/music-files | PUT /api/music-files/{id} |

## Upload trong Edit Mode

### Trường hợp 1: Không upload file mới
- Giữ nguyên: `fileCode`, `downloadLink`, `filePath`, `fileType`
- Cập nhật: metadata khác từ form

### Trường hợp 2: Upload file mới
- Sử dụng: `fileCode`, `downloadLink` mới từ upload response
- Auto-fill: `artist`, `album`, `duration`, `fileSize` từ metadata
- User có thể chỉnh sửa thêm trước khi submit

## API Request Example

### Create
```json
POST /api/music-files
{
  "fileCode": "UPLOADED_FILE_CODE",
  "fileName": "Tên bài hát",
  "genreId": 1,
  "filePath": "/storage/music/...",
  "fileType": "mp3",
  "downloadLink": "/api/music-files/download/...",
  "artist": "Sơn Tùng MTP",
  "album": "Sky Tour",
  "duration": 240,
  "fileSize": 5242880,
  "releaseYear": 2020,
  "description": "Mô tả"
}
```

### Update
```json
PUT /api/music-files/5
{
  "fileCode": "EXISTING_OR_NEW_FILE_CODE",
  "fileName": "Tên bài hát (đã sửa)",
  "genreId": 2,
  "filePath": "/storage/music/...",
  "fileType": "mp3",
  "downloadLink": "/api/music-files/download/...",
  "artist": "Nghệ sĩ mới",
  "album": "Album mới",
  "duration": 250,
  "fileSize": 6000000,
  "releaseYear": 2021,
  "description": "Mô tả mới"
}
```

## Test Checklist

### Create Mode
- [ ] Form hiển thị rỗng khi mở
- [ ] Bắt buộc chọn file
- [ ] Upload file thành công
- [ ] Auto-fill artist, album, duration, size từ upload
- [ ] Submit tạo mới thành công
- [ ] Navigate back sau khi tạo

### Edit Mode
- [ ] Load dữ liệu từ API
- [ ] Form hiển thị đúng thông tin
- [ ] Dropdown genre chọn đúng
- [ ] Có thể chỉnh sửa trường text
- [ ] Không bắt buộc chọn file mới
- [ ] Nếu chọn file mới → Upload và auto-fill
- [ ] Submit cập nhật thành công
- [ ] Navigate back sau khi cập nhật

### Edge Cases
- [ ] Edit nhưng API ID không tồn tại
- [ ] Upload file mới khi edit nhưng upload thất bại
- [ ] Genre không tồn tại trong danh sách
- [ ] Network error khi load detail
- [ ] Network error khi submit

---
**Ngày hoàn thành**: 2025-11-14
**Status**: ✅ Hoàn tất

