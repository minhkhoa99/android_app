# ⚠️ HIỆN TẠI: APP CHƯA UPLOAD FILE THẬT

## ❌ Vấn Đề

App **KHÔNG** call API `http://10.0.2.2:3005/api/music-files/upload` để upload file.

### Điều App Đang Làm:
```
1. User chọn file → fileUri = "content://..."
2. Tạo metadata: 
   POST /api/music-files
   Body: {
     fileCode: "...",
     fileName: "...",
     filePath: "content://...",  ← URI LOCAL, không phải file trên server
     genreId: 4,
     ...
   }
3. Server lưu metadata nhưng KHÔNG có file thật
```

### Kết Quả:
- ✅ Metadata được lưu trong database
- ❌ File KHÔNG được upload lên server
- ❌ `filePath` là URI local, không truy cập được từ server
- ❌ Không thể download file

---

## ✅ GIẢI PHÁP: Đã Thêm Upload API

### 1. **Đã Thêm API Endpoint**

**File:** `MusicApiService.kt`

```kotlin
@Multipart
@POST("music-files/upload")
suspend fun uploadMusicFile(
    @Part file: okhttp3.MultipartBody.Part,
    @Part("genreId") genreId: okhttp3.RequestBody,
    @Part("artist") artist: okhttp3.RequestBody? = null,
    @Part("album") album: okhttp3.RequestBody? = null,
    @Part("releaseYear") releaseYear: okhttp3.RequestBody? = null,
    @Part("description") description: okhttp3.RequestBody? = null
): Response<MusicFileResponse>
```

### 2. **Đã Thêm Repository Function**

**File:** `MusicApiRepository.kt`

```kotlin
suspend fun uploadMusicFile(
    file: java.io.File,
    genreId: Int,
    artist: String? = null,
    album: String? = null,
    releaseYear: Int? = null,
    description: String? = null
): ApiResult<Music>
```

### 3. **Đã Thêm ViewModel Function**

**File:** `MusicViewModel.kt`

```kotlin
fun uploadMusicFile(
    file: java.io.File,
    genreId: Int,
    artist: String? = null,
    album: String? = null,
    releaseYear: Int? = null,
    description: String? = null
)
```

---

## 🔧 CÁCH SỬ DỤNG (Cần Implement Trong AddMusicScreen)

### **Bước 1: Convert URI sang File**

```kotlin
// Helper function để copy file từ URI sang temp file
fun Context.getFileFromUri(uri: Uri): File? {
    return try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val fileName = uri.lastPathSegment ?: "temp_${System.currentTimeMillis()}"
        val tempFile = File(cacheDir, fileName)
        
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
```

### **Bước 2: Gọi Upload Thay Vì Create**

**Trong AddMusicScreen.kt:**

```kotlin
// ❌ TRƯỚC (Chỉ tạo metadata):
musicViewModel.createMusicFile(
    fileCode = fileCode,
    fileName = title,
    genreId = genreApiId,
    ...
)

// ✅ SAU (Upload file thật):
val context = LocalContext.current
val file = fileUri?.let { context.getFileFromUri(it) }

if (file != null) {
    musicViewModel.uploadMusicFile(
        file = file,
        genreId = genreApiId,
        artist = artist.ifBlank { null },
        album = album.ifBlank { null },
        releaseYear = releaseYear,
        description = description.ifBlank { null }
    )
} else {
    localError = "Không thể đọc file. Vui lòng chọn lại."
}
```

---

## 📊 So Sánh Flow

### **HIỆN TẠI (Metadata Only):**
```
User chọn file
  ↓
fileUri = "content://..."
  ↓
POST /api/music-files
Body: {
  filePath: "content://...",  ← LOCAL URI
  fileName: "song.mp3",
  ...
}
  ↓
Server lưu metadata
  ↓
❌ File KHÔNG có trên server
```

### **SAU KHI SỬA (File Upload):**
```
User chọn file
  ↓
fileUri = "content://..."
  ↓
Convert URI → File object
  ↓
POST /api/music-files/upload (Multipart)
Parts:
  - file: [Binary Data]
  - genreId: "4"
  - artist: "Tim"
  - ...
  ↓
Server:
  1. Save file → /uploads/music/2025/11/song.mp3
  2. Save metadata → DB
  ↓
Response: {
  filePath: "/uploads/music/2025/11/song.mp3",  ← SERVER PATH
  downloadLink: "http://...:3005/uploads/music/2025/11/song.mp3"
}
  ↓
✅ File CÓ SẴN trên server, có thể download
```

---

## 🔐 Permissions Cần Thiết

### **AndroidManifest.xml:**
```xml
<!-- Đọc file từ storage -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

<!-- Internet -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

## ⚠️ TODO: CẦN IMPLEMENT

### **AddMusicScreen.kt - Cần Thêm:**

1. ✅ **Import Context:**
   ```kotlin
   import androidx.compose.ui.platform.LocalContext
   ```

2. ✅ **Helper Function:**
   ```kotlin
   fun Context.getFileFromUri(uri: Uri): File? { ... }
   ```

3. ✅ **Sửa Logic Button "Lưu":**
   ```kotlin
   if (fileUri != null) {
       val context = LocalContext.current
       val file = context.getFileFromUri(fileUri)
       
       if (file != null) {
           musicViewModel.uploadMusicFile(...)
       } else {
           localError = "Không thể đọc file"
       }
   }
   ```

4. ✅ **Loading Progress:**
   ```kotlin
   if (isLoading) {
       LinearProgressIndicator(
           modifier = Modifier.fillMaxWidth(),
           progress = uploadProgress  // 0.0 - 1.0
       )
   }
   ```

---

## 🧪 Test API Upload

### **Với Postman/cURL:**

```bash
curl -X POST http://localhost:3005/api/music-files/upload \
  -F "file=@/path/to/song.mp3" \
  -F "genreId=4" \
  -F "artist=Tim" \
  -F "album=demo" \
  -F "releaseYear=2020" \
  -F "description=test upload"
```

**Expected Response:**
```json
{
  "id": 10,
  "fileCode": "SONG_1234567890",
  "fileName": "song.mp3",
  "filePath": "/uploads/music/2025/11/song_1234567890.mp3",
  "downloadLink": "http://localhost:3005/uploads/music/2025/11/song_1234567890.mp3",
  "genreId": 4,
  "artist": "Tim",
  ...
}
```

---

## 📈 Lợi Ích Khi Upload File Thật

| Feature | Metadata Only | File Upload |
|---------|---------------|-------------|
| **Lưu metadata** | ✅ | ✅ |
| **File trên server** | ❌ | ✅ |
| **Download được** | ❌ | ✅ |
| **Play music** | ❌ | ✅ |
| **Share link** | ❌ | ✅ |
| **Backup** | ❌ | ✅ |

---

## ✅ Kết Luận

### **Hiện Tại:**
- ❌ App **CHƯA** upload file
- ❌ Chỉ tạo metadata với URI local
- ❌ Server không có file thật

### **Đã Chuẩn Bị:**
- ✅ API endpoint `/upload` đã có
- ✅ Repository function đã có
- ✅ ViewModel function đã có

### **Cần Làm Tiếp:**
1. ⏳ Thêm `getFileFromUri()` helper
2. ⏳ Sửa AddMusicScreen gọi `uploadMusicFile()`
3. ⏳ Test upload với file thật
4. ⏳ Verify file được lưu trên server

---

## 🚀 Bước Tiếp Theo

**Có 2 lựa chọn:**

### **Option 1: Chỉ Metadata (Hiện Tại)**
- ✅ Nhanh, đơn giản
- ❌ Không có file thật
- ❌ Không play được

### **Option 2: Upload File Thật (Recommended)**
- ✅ Đầy đủ tính năng
- ✅ Play music được
- ✅ Download được
- ⏳ Cần implement thêm code

**Nếu muốn upload file thật, cần implement các TODO ở trên!** 🎵

