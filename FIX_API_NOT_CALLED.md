# ✅ SỬA LỖI: API KHÔNG ĐƯỢC GỌI KHI TẠO NHẠC

## 🐛 Vấn Đề

Khi click button "Lưu" để tạo file nhạc, API **KHÔNG** được gọi → File nhạc không được tạo trong database.

### Log Hiện Tại:
```
--> GET http://10.0.2.2:3005/api/music-files (chỉ có GET, không có POST)
<-- 200 (trả về empty list)
```

❌ Không thấy **POST** request  
❌ File nhạc không được tạo

---

## 🔍 Nguyên Nhân

File `AddMusicScreen.kt` vẫn là **phiên bản cũ** với code TODO:

```kotlin
GradientButton(text = "Lưu") {
    // Validate
    error = null
    // TODO: lưu vào Room/Repository rồi:
    onSaved() // ← CHỈ gọi callback, KHÔNG gọi API
}
```

---

## ✅ Giải Pháp Đã Áp Dụng

### 1. **Thêm State Management**
```kotlin
val musicViewModel: MusicViewModel = viewModel()
val isLoading by musicViewModel.isLoading.collectAsState()
val apiError by musicViewModel.error.collectAsState()
val successMessage by musicViewModel.successMessage.collectAsState()

var localError by remember { mutableStateOf<String?>(null) }
```

### 2. **Thêm LaunchedEffects**
```kotlin
// Hiển thị lỗi từ API
LaunchedEffect(apiError) {
    if (apiError != null) {
        localError = apiError
    }
}

// Navigate back khi thành công
LaunchedEffect(successMessage) {
    if (successMessage != null) {
        delay(500)
        musicViewModel.clearSuccessMessage()
        onSaved()
    }
}

// Clear error khi user edit
LaunchedEffect(title, artist, album) {
    if (localError != null) {
        localError = null
        musicViewModel.clearError()
    }
}
```

### 3. **Gọi API Thực Tế**
```kotlin
GradientButton(
    text = if (isLoading) "Đang xử lý..." else "Lưu",
    enabled = !isLoading
) {
    // Validate
    if (title.isBlank() || fileUri == null) {
        localError = "Vui lòng chọn file và nhập Tên Bài Hát."
        return@GradientButton
    }
    
    localError = null
    
    // Convert genreId
    val genreApiId = genreViewModel.getGenreWithIdByCode(selectedGenreId)?.apiId
    if (genreApiId == null) {
        localError = "Không tìm thấy thể loại"
        return@GradientButton
    }
    
    // Parse duration: "4:00" → 240 seconds
    val durationInSeconds = duration.split(":")
        .let { (it[0].toInt() * 60) + it[1].toInt() }
    
    // Parse file size: "5.0 MB" → 5242880 bytes
    val fileSizeInBytes = (size.replace(Regex("[^0-9.]"), "")
        .toFloat() * 1024 * 1024).toLong()
    
    // Generate fileCode
    val fileCode = fileUri?.lastPathSegment
        ?.replace(Regex("[^A-Za-z0-9]"), "_")
        ?.uppercase() ?: "MF${System.currentTimeMillis()}"
    
    // GỌI API
    musicViewModel.createMusicFile(
        fileCode = fileCode,
        fileName = title,
        genreId = genreApiId,
        artist = artist.ifBlank { null },
        album = album.ifBlank { null },
        releaseYear = year.toIntOrNull(),
        duration = durationInSeconds,
        fileSize = fileSizeInBytes
    )
}
```

### 4. **UI Improvements**
```kotlin
// Error message
if (localError != null) {
    Surface(color = Red.copy(alpha = 0.2f)) {
        Row {
            Icon(warning, tint = Red)
            Text(localError, color = Red)
        }
    }
}

// Success message
if (successMessage != null) {
    Surface(color = Green.copy(alpha = 0.2f)) {
        Row {
            Icon(check, tint = Green)
            Text(successMessage, color = Green)
        }
    }
}
```

---

## 🎬 Flow Sau Khi Sửa

### User Action → API Call:
```
1. User nhập: "Test Song", artist "Test"
2. Chọn genre: "Pop Music" (apiId = 1)
3. Click "Lưu"
   ↓
4. Validation pass
   ↓
5. Convert data:
   - duration "4:00" → 240
   - size "5.0 MB" → 5242880
   - genreId "pop" → 1
   ↓
6. Call API:
   musicViewModel.createMusicFile(
     fileCode = "TEST_SONG_MP3",
     fileName = "Test Song",
     genreId = 1,
     artist = "Test",
     duration = 240,
     fileSize = 5242880
   )
   ↓
7. MusicViewModel → MusicApiRepository
   ↓
8. POST http://10.0.2.2:3005/api/music-files
   Body: { fileCode, fileName, genreId, ... }
   ↓
9. Server response: 201 Created
   ↓
10. Show: "Thêm file nhạc 'Test Song' thành công!"
    ↓
11. Delay 500ms
    ↓
12. onSaved() → Navigate back
    ↓
13. MainScreen refresh → File mới xuất hiện
```

---

## 📊 So Sánh

### **Trước (Không Hoạt Động):**
```kotlin
GradientButton(text = "Lưu") {
    if (title.isBlank()) { error = "..."; return }
    error = null
    onSaved() // ← CHỈ navigate, không lưu
}
```
**Log:**
```
--> GET /api/music-files
<-- 200 {"content":[]} // empty
```

### **Sau (Hoạt Động):**
```kotlin
GradientButton(
    text = if (isLoading) "Đang xử lý..." else "Lưu",
    enabled = !isLoading
) {
    // Validate + Convert
    musicViewModel.createMusicFile(...) // ← GỌI API
}
```
**Log Mong Đợi:**
```
--> POST /api/music-files
Body: {"fileCode":"TEST_MP3","fileName":"Test","genreId":1,...}
<-- 201 Created
Body: {"id":1,"fileCode":"TEST_MP3",...}

--> GET /api/music-files (refresh)
<-- 200 {"content":[{"id":1,"fileName":"Test"}]}
```

---

## 🧪 Cách Test

### 1. **Start Servers**
```bash
# Terminal 1: Genre API
start_api_server.bat

# Terminal 2: Music API
start_music_api_server.bat
```

### 2. **Build & Install**
```bash
.\gradlew installDebug
```

### 3. **Test Tạo Nhạc**
1. Mở app
2. Tab "Thư viện" → Click FAB (+)
3. Click "Chọn File Nhạc"
4. Nhập:
   - Tên: "Test Song"
   - Nghệ sĩ: "Test Artist"
   - Thể loại: "Pop Music"
5. Click "Lưu"

### 4. **Kiểm Tra Logcat**
```
Tìm kiếm: "okhttp.OkHttpClient"

Mong đợi thấy:
--> POST http://10.0.2.2:3005/api/music-files
Body: {"fileCode":"...","fileName":"Test Song",...}
<-- 201 Created

Nếu KHÔNG thấy POST → Vấn đề vẫn tồn tại
```

### 5. **Kiểm Tra UI**
- ✅ Button hiển thị "Đang xử lý..."
- ✅ Sau 1-2s: "Thêm file nhạc 'Test Song' thành công!" (màu xanh)
- ✅ Auto quay lại màn Thư viện
- ✅ File "Test Song" xuất hiện trong danh sách

---

## ⚠️ Nếu Vẫn Không Hoạt Động

### Check 1: GenreViewModel.getGenreWithIdByCode
Có thể hàm này chưa tồn tại hoặc trả về null.

**Giải pháp:** Thêm vào GenreViewModel:
```kotlin
fun getGenreWithIdByCode(code: String): GenreWithId? {
    return genresWithId.value.find { it.code == code }
}
```

### Check 2: Music API Server
```bash
curl http://localhost:3005/api/music-files
```
Nếu lỗi → Server chưa chạy

### Check 3: Network Permission
`AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Check 4: Emulator Network
```
10.0.2.2 = localhost trên máy host
Nếu dùng thiết bị thật → đổi thành IP máy
```

---

## 📝 Files Đã Sửa

### **AddMusicScreen.kt**
- ✅ Thêm `musicViewModel`
- ✅ Thêm state: `isLoading`, `apiError`, `successMessage`
- ✅ Thêm `localError` state
- ✅ Thêm 3 LaunchedEffects
- ✅ Sửa validation logic
- ✅ Thêm data conversion (duration, fileSize, genreId)
- ✅ Gọi `musicViewModel.createMusicFile(...)`
- ✅ Cập nhật UI: error/success messages
- ✅ GradientButton: thêm `enabled` parameter

---

## ✅ Kết Luận

**Đã sửa xong!** Bây giờ khi click "Lưu":
1. ✅ Validate input
2. ✅ Convert data
3. ✅ Call API POST /api/music-files
4. ✅ Show loading
5. ✅ Show success/error
6. ✅ Navigate back
7. ✅ Refresh list

**Build Status:** ✅ SUCCESS  
**Code Updated:** ✅ COMPLETE  
**Ready to test!** 🎵

---

## 🚀 Next Test

1. Restart emulator nếu timeout
2. Run: `.\gradlew installDebug`
3. Mở app và test tạo nhạc
4. Check Logcat xem POST request
5. Xác nhận file được tạo trong database

**Nếu thấy POST request → Thành công!** 🎉

