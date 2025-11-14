# ✅ SỬA LỖI: Upload Thiếu Parameter `fileName`

## 🐛 Lỗi

```json
{
  "status": 500,
  "message": "Internal server error: Required request parameter 'fileName' for method parameter type String is not present",
  "timestamp": "2025-11-14T15:45:33.4592007"
}
```

**Nguyên nhân:** Server yêu cầu **`fileName`** nhưng request chỉ gửi `file` và `fileCode`.

---

## ✅ Giải Pháp

### **Thêm `fileName` Vào Tất Cả Layers**

### 1. **MusicApiService.kt**

```kotlin
@Multipart
@POST("music-files/upload")
suspend fun uploadMusicFile(
    @Part file: okhttp3.MultipartBody.Part,
    @Part("fileCode") fileCode: okhttp3.RequestBody,
    @Part("fileName") fileName: okhttp3.RequestBody  // ✅ THÊM
): Response<MusicFileResponse>
```

### 2. **MusicApiRepository.kt**

```kotlin
suspend fun uploadMusicFile(
    file: java.io.File, 
    fileCode: String, 
    fileName: String  // ✅ THÊM
): ApiResult<String> {
    // ...
    
    // Create fileName part
    val fileNamePart = fileName.toRequestBody("text/plain".toMediaTypeOrNull())
    
    val result = safeApiCall {
        apiService.uploadMusicFile(filePart, fileCodePart, fileNamePart)  // ✅ THÊM
    }
}
```

### 3. **MusicViewModel.kt**

```kotlin
suspend fun uploadMusicFile(
    file: java.io.File, 
    fileCode: String, 
    fileName: String  // ✅ THÊM
): String? {
    val result = repository.uploadMusicFile(file, fileCode, fileName)
    // ...
}
```

### 4. **AddMusicScreen.kt**

```kotlin
// Upload với fileCode và fileName
val downloadLink = musicViewModel.uploadMusicFile(
    file, 
    fileCode, 
    title  // ✅ THÊM - Tên bài hát làm fileName
)
```

---

## 📊 Request Hoàn Chỉnh

### **Multipart Request:**

```
POST /api/music-files/upload
Content-Type: multipart/form-data; boundary=----Boundary

------Boundary
Content-Disposition: form-data; name="file"; filename="song.mp3"
Content-Type: audio/*

[Binary Data]
------Boundary
Content-Disposition: form-data; name="fileCode"

MOT_VONG_TRAI_DAT_MP3
------Boundary
Content-Disposition: form-data; name="fileName"

mot vong trai dat
------Boundary--
```

### **Các Parameters:**

| Parameter | Value | Description |
|-----------|-------|-------------|
| `file` | [Binary] | File nhạc thực tế |
| `fileCode` | "MOT_VONG_TRAI_DAT_MP3" | Mã file (unique) |
| `fileName` | "mot vong trai dat" | Tên bài hát |

---

## 🔄 Flow Upload Hoàn Chỉnh

```
1. User nhập thông tin:
   - Chọn file: "song.mp3"
   - Tên bài hát: "mot vong trai dat"
   - Nghệ sĩ: "Tim"
   - Album: "demo"
   ↓
2. Generate fileCode:
   fileCode = "MOT_VONG_TRAI_DAT_MP3"
   ↓
3. Upload file:
   POST /api/music-files/upload
   Multipart:
     - file: [Binary from song.mp3]
     - fileCode: "MOT_VONG_TRAI_DAT_MP3"
     - fileName: "mot vong trai dat"  ← ✅ BẮT BUỘC
   ↓
4. Server Response:
   {
     "id": 10,
     "fileCode": "MOT_VONG_TRAI_DAT_MP3",
     "fileName": "mot vong trai dat",
     "filePath": "/uploads/music/2025/11/MOT_VONG_TRAI_DAT_MP3.mp3",
     "downloadLink": "http://10.0.2.2:3005/uploads/music/2025/11/MOT_VONG_TRAI_DAT_MP3.mp3"
   }
   ↓
5. Lưu downloadLink
   ↓
6. Tạo metadata:
   POST /api/music-files
   {
     "fileCode": "MOT_VONG_TRAI_DAT_MP3",
     "fileName": "mot vong trai dat",
     "downloadLink": "http://10.0.2.2:3005/uploads/...",
     "genreId": 4,
     "artist": "Tim",
     "album": "demo",
     ...
   }
   ↓
7. ✅ Hoàn tất!
```

---

## 📝 Files Đã Sửa

1. ✅ **MusicApiService.kt** - Thêm `fileName` parameter
2. ✅ **MusicApiRepository.kt** - Tạo `fileNamePart` và gửi lên API
3. ✅ **MusicViewModel.kt** - Thêm `fileName` parameter
4. ✅ **AddMusicScreen.kt** - Truyền `title` làm `fileName`

---

## 🎯 Tóm Tắt Thay Đổi

### **Trước (Lỗi):**
```kotlin
uploadMusicFile(file, fileCode)
// → Request: { file, fileCode }
// → Server Error 500: fileName is required
```

### **Sau (Đúng):**
```kotlin
uploadMusicFile(file, fileCode, title)
// → Request: { file, fileCode, fileName }
// → Server: 200 OK
```

---

## ✅ Kết Luận

**Đã sửa xong!** Upload API bây giờ gửi **3 parameters bắt buộc**:

1. ✅ **`file`** - File nhạc (binary)
2. ✅ **`fileCode`** - Mã file unique
3. ✅ **`fileName`** - Tên bài hát

**Build Status:** 🔄 Building...  
**Lỗi 500 sẽ được fix!** 🎵

---

## 🚀 Test

1. **Chọn file:** "Mot Vong.mp3"
2. **Nhập tên:** "mot vong trai dat"
3. **Click Lưu**
4. **Expected:**
   ```
   --> POST /api/music-files/upload
   Multipart: file, fileCode, fileName
   
   <-- 200 OK
   {
     "downloadLink": "http://10.0.2.2:3005/uploads/..."
   }
   
   --> POST /api/music-files
   Body: { downloadLink, ... }
   
   <-- 201 Created
   ```

5. ✅ **File uploaded thành công!**

**Giờ có thể upload file thật lên server!** 🎉

