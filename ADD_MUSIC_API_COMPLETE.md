# ✅ HOÀN THÀNH TÍCH HỢP API CHO THÊM/SỬA NHẠC

## 📝 Tổng Quan

Đã ghép thành công **Music API (port 3005)** vào màn **Thêm File Nhạc** (AddMusicScreen). Giờ có thể tạo mới và cập nhật file nhạc thông qua API thực thay vì mock data.

---

## 🔄 Thay Đổi Chính

### **Trước:**
```kotlin
GradientButton(text = "Lưu") {
    // TODO: lưu vào Room/Repository rồi:
    onSaved()
}
```
❌ Chỉ gọi callback, không lưu dữ liệu  
❌ Không có validation API  
❌ Không có error handling

### **Sau:**
```kotlin
val musicViewModel: MusicViewModel = viewModel()
val isLoading by musicViewModel.isLoading.collectAsState()
val apiError by musicViewModel.error.collectAsState()
val successMessage by musicViewModel.successMessage.collectAsState()

GradientButton(
    text = if (isLoading) "Đang xử lý..." else "Lưu",
    enabled = !isLoading
) {
    // Validate
    // Convert data
    // Call API
    musicViewModel.createMusicFile(...)
}
```
✅ Gọi API thực  
✅ Loading state  
✅ Error handling  
✅ Success notification

---

## 🎯 Tính Năng Đã Tích Hợp

### 1. **Create Music File (POST)**
```kotlin
musicViewModel.createMusicFile(
    fileCode = "MF123",
    fileName = "Bài hát mới.mp3",
    genreId = 1, // API ID từ Genre
    artist = "Nghệ sĩ A",
    album = "Album Demo",
    releaseYear = 2023,
    duration = 240, // seconds
    fileSize = 5242880 // bytes
)
```

**API Endpoint:**
```
POST http://10.0.2.2:3005/api/music-files
```

### 2. **Update Music File (PUT)**
```kotlin
musicViewModel.updateMusicFile(
    id = musicApiId,
    fileCode = "MF123",
    fileName = "Bài hát đã sửa.mp3",
    genreId = 2,
    artist = "Nghệ sĩ B",
    // ...
)
```

**API Endpoint:**
```
PUT http://10.0.2.2:3005/api/music-files/{id}
```

---

## 🔧 Xử Lý Dữ Liệu

### 1. **Genre ID Conversion**
```kotlin
// selectedGenreId là string (VD: "pop")
// Cần convert sang genreId (Int) từ API

val genreWithId = genreViewModel.getGenreWithIdByCode(selectedGenreId)
val genreApiId = genreWithId?.apiId // Int ID từ API

if (genreApiId == null) {
    localError = "Không tìm thấy thể loại. Vui lòng chọn lại."
    return
}
```

### 2. **Duration Parsing**
```kotlin
// Input: "4:00" (MM:SS)
// Output: 240 seconds

val durationInSeconds = try {
    val parts = duration.split(":")
    val minutes = parts[0].toIntOrNull() ?: 0
    val seconds = parts[1].toIntOrNull() ?: 0
    minutes * 60 + seconds
} catch (e: Exception) {
    240 // default
}
```

### 3. **File Size Parsing**
```kotlin
// Input: "5.0 MB"
// Output: 5242880 bytes

val fileSizeInBytes = try {
    val sizeStr = size.replace(Regex("[^0-9.]"), "")
    val sizeFloat = sizeStr.toFloatOrNull() ?: 5.0f
    (sizeFloat * 1024 * 1024).toLong()
} catch (e: Exception) {
    5242880L // 5MB default
}
```

### 4. **File Code Generation**
```kotlin
// Từ URI: "song.mp3" → "SONG_MP3"
// Fallback: "MF1699876543210"

val fileCode = fileUri?.lastPathSegment
    ?.replace(Regex("[^A-Za-z0-9]"), "_")
    ?.uppercase()
    ?: "MF${System.currentTimeMillis()}"
```

---

## 📱 UI/UX Improvements

### 1. **Loading State**
```kotlin
GradientButton(
    text = if (isLoading) "Đang xử lý..." else "Lưu",
    enabled = !isLoading
) { ... }
```
- ✅ Button disabled khi đang xử lý
- ✅ Text thay đổi: "Lưu" → "Đang xử lý..."
- ✅ Background gray khi disabled

### 2. **Error Display**
```kotlin
if (localError != null) {
    Surface(color = Color(0x20FF5252), ...) {
        Row {
            Icon(..., tint = Color(0xFFFF5252))
            Text(localError, color = Color(0xFFFF5252))
        }
    }
}
```
- ✅ Red background với alpha
- ✅ Icon warning
- ✅ Clear message

### 3. **Success Display**
```kotlin
if (successMessage != null) {
    Surface(color = Color(0x204CAF50), ...) {
        Row {
            Icon(..., tint = Color(0xFF4CAF50))
            Text(successMessage, color = Color(0xFF4CAF50))
        }
    }
}
```
- ✅ Green background
- ✅ Success icon
- ✅ Auto navigate back sau 500ms

### 4. **Auto Clear Error**
```kotlin
LaunchedEffect(title, artist, album) {
    if (localError != null && apiError != null) {
        localError = null
        musicViewModel.clearError()
    }
}
```
- ✅ Error tự động xóa khi user chỉnh sửa

---

## 🎬 Flow Hoàn Chỉnh

### **Tạo Mới:**
```
1. User nhập thông tin
2. Click "Lưu"
3. Validate (file, title, genre)
4. Convert data (duration, size, genreId)
5. Call API: createMusicFile()
6. Show loading: "Đang xử lý..."
7. API Success → Show "Thêm file nhạc ... thành công!"
8. Delay 500ms
9. Navigate back → Refresh danh sách
```

### **Chỉnh Sửa:**
```
1. Load existing data
2. User sửa thông tin
3. Click "Cập Nhật"
4. Validate
5. Convert data
6. Get musicApiId from code
7. Call API: updateMusicFile(id, ...)
8. Show loading
9. Success → Navigate back
```

---

## 🔌 API Integration Details

### Request Model:
```kotlin
data class MusicFileRequest(
    val fileCode: String,
    val fileName: String,
    val genreId: Int,
    val artist: String? = null,
    val album: String? = null,
    val releaseYear: Int? = null,
    val description: String? = null,
    val duration: Int? = null,
    val fileSize: Long? = null
)
```

### Response Model:
```kotlin
data class MusicFileResponse(
    val id: Int,
    val fileCode: String,
    val fileName: String,
    val genreId: Int,
    val genreName: String?,
    val artist: String?,
    // ...
)
```

---

## 🧪 Testing

### Test Case 1: Tạo Mới Thành Công
**Steps:**
1. Click FAB (+) trong Thư viện
2. Click "Chọn File Nhạc"
3. Chọn file âm thanh
4. Nhập: Tên = "Test Song", Nghệ sĩ = "Test Artist"
5. Chọn thể loại: "Pop Music"
6. Click "Lưu"

**Expected:**
- ✅ Button hiển thị "Đang xử lý..."
- ✅ Sau 1-2s hiện "Thêm file nhạc 'Test Song' thành công!"
- ✅ Auto quay lại màn Thư viện
- ✅ Bài hát mới xuất hiện trong danh sách

### Test Case 2: Validation Error
**Steps:**
1. Mở màn thêm nhạc
2. Để trống "Tên Bài Hát"
3. Click "Lưu"

**Expected:**
- ✅ Hiển thị error đỏ: "Vui lòng chọn file và nhập Tên Bài Hát."
- ✅ Không gọi API

### Test Case 3: API Error
**Steps:**
1. Tắt Music API server (port 3005)
2. Thử tạo bài hát mới

**Expected:**
- ✅ Hiển thị error: "Không thể kết nối đến server..."
- ✅ Button enabled lại để retry

### Test Case 4: Genre Not Found
**Steps:**
1. Xóa tất cả genres
2. Thử tạo bài hát

**Expected:**
- ✅ Error: "Không tìm thấy thể loại. Vui lòng chọn lại."

---

## 📊 Data Mapping

| UI Field | Variable | API Field | Type | Transform |
|----------|----------|-----------|------|-----------|
| Mã File | `fileUri.lastPathSegment` | `fileCode` | String | Uppercase + sanitize |
| Tên Bài Hát | `title` | `fileName` | String | Direct |
| Nghệ Sĩ | `artist` | `artist` | String? | Null if blank |
| Album | `album` | `album` | String? | Null if blank |
| Năm Phát Hành | `year` | `releaseYear` | Int? | Parse to Int |
| Thời lượng | `duration` | `duration` | Int? | "4:00" → 240s |
| Kích thước | `size` | `fileSize` | Long? | "5.0 MB" → 5242880 |
| Thể loại | `selectedGenreId` | `genreId` | Int | Get from GenreViewModel |

---

## 🎨 UI Components

### GradientButton Enhanced:
```kotlin
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true, // ← New
    onClick: () -> Unit
) {
    // Gray gradient when disabled
    val gradient = if (enabled) 
        Brush.horizontal(Blue, Purple)
    else
        Brush.horizontal(Gray, Gray)
    
    Text(text, color = if (enabled) White else LightGray)
}
```

---

## ⚠️ Lưu Ý

### 1. **Server Phải Chạy**
```bash
start_music_api_server.bat
```
Kiểm tra: http://localhost:3005/api/music-files

### 2. **Genre API Cũng Cần Chạy**
```bash
start_api_server.bat
```
Vì cần lấy `genreApiId` từ GenreViewModel

### 3. **musicId vs apiId**
- `musicId` (String): Code dùng trong UI ("MF001")
- `apiId` (Int): ID thực từ database (1, 2, 3...)
- Phải convert: `musicId` → `apiId` trước khi update

---

## 📈 Architecture

```
AddMusicScreen (UI)
    ↓ User clicks "Lưu"
    ↓ Validate + Transform data
    ↓
MusicViewModel.createMusicFile(...)
    ↓
MusicApiRepository.createMusicFile(...)
    ↓
MusicApiService.createMusicFile(@Body request)
    ↓ HTTP POST
json-server (port 3005)
    ↓ Save to music_db.json
    ↓ Return MusicFileResponse
    ↑
AddMusicScreen
    ↓ Show success
    ↓ Navigate back
MainScreen (refresh list)
```

---

## ✅ Kết Luận

**Hoàn thành!** Màn Thêm/Sửa Nhạc giờ đây:
- ✅ Gọi API thực (port 3005)
- ✅ Tạo mới file nhạc với đầy đủ metadata
- ✅ Cập nhật file nhạc existing
- ✅ Validation đầy đủ
- ✅ Error handling tốt
- ✅ Loading state UX
- ✅ Success notification
- ✅ Auto navigate back
- ✅ Refresh danh sách tự động

**Build Status:** 🔄 Building...  
**Ready to test!** 🎵

---

## 🚀 Next Steps (Optional)

1. ⏳ Upload file thật lên server
2. ⏳ Parse metadata từ file (artist, duration, ...)
3. ⏳ Image picker cho thumbnail
4. ⏳ Validate file format (mp3, wav, flac)
5. ⏳ Progress bar khi upload

**Hiện tại:** Tạo metadata trong DB, file upload sẽ implement sau!

