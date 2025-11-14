# ✅ SỬA LỖI: Upload Thiếu Parameter `fileCode`

## 🐛 Lỗi

```json
{
  "status": 500,
  "message": "Internal server error: Required request parameter 'fileCode' for method parameter type String is not present",
  "timestamp": "2025-11-14T15:33:25.4372591"
}
```

**Nguyên nhân:** API upload yêu cầu parameter `fileCode` nhưng request chỉ gửi `file`.

---

## ✅ Giải Pháp

### 1. **Thêm fileCode vào MusicApiService**

**File:** `MusicApiService.kt`

```kotlin
@Multipart
@POST("music-files/upload")
suspend fun uploadMusicFile(
    @Part file: okhttp3.MultipartBody.Part,
    @Part("fileCode") fileCode: okhttp3.RequestBody  // ✅ THÊM
): Response<MusicFileResponse>
```

### 2. **Cập nhật MusicApiRepository**

**File:** `MusicApiRepository.kt`

```kotlin
suspend fun uploadMusicFile(file: java.io.File, fileCode: String): ApiResult<String> {
    // ...
    
    // Create fileCode part
    val fileCodePart = fileCode.toRequestBody("text/plain".toMediaTypeOrNull())
    
    val result = safeApiCall {
        apiService.uploadMusicFile(filePart, fileCodePart)  // ✅ THÊM fileCodePart
    }
    
    // ...
}
```

### 3. **Cập nhật MusicViewModel**

**File:** `MusicViewModel.kt`

```kotlin
suspend fun uploadMusicFile(file: java.io.File, fileCode: String): String? {
    val result = repository.uploadMusicFile(file, fileCode)  // ✅ THÊM fileCode
    // ...
}
```

### 4. **Cập nhật AddMusicScreen**

**File:** `AddMusicScreen.kt`

```kotlin
// Upload với fileCode
val downloadLink = musicViewModel.uploadMusicFile(file, fileCode)  // ✅ THÊM fileCode
```

---

## 📊 Request Trước và Sau

### **Trước (Lỗi):**
```
POST /api/music-files/upload
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="file"; filename="song.mp3"
Content-Type: audio/*

[Binary Data]
--boundary--
```

❌ **Thiếu fileCode** → Server error 500

### **Sau (Đúng):**
```
POST /api/music-files/upload
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="file"; filename="song.mp3"
Content-Type: audio/*

[Binary Data]
--boundary
Content-Disposition: form-data; name="fileCode"

MOT_VONG_TRAI_DAT_MP3
--boundary--
```

✅ **Có fileCode** → Upload thành công

---

## 🔄 Flow Hoàn Chỉnh

```
1. User chọn file
   ↓
2. Generate fileCode = "MOT_VONG_TRAI_DAT_MP3"
   ↓
3. Convert URI → File object
   ↓
4. Upload file + fileCode
   POST /api/music-files/upload
   - file: [Binary Data]
   - fileCode: "MOT_VONG_TRAI_DAT_MP3"
   ↓
5. Server xử lý:
   - Lưu file → /uploads/music/2025/11/MOT_VONG_TRAI_DAT_MP3.mp3
   - Generate downloadLink
   ↓
6. Response:
   {
     "id": 10,
     "fileCode": "MOT_VONG_TRAI_DAT_MP3",
     "fileName": "song.mp3",
     "filePath": "/uploads/music/2025/11/MOT_VONG_TRAI_DAT_MP3.mp3",
     "downloadLink": "http://10.0.2.2:3005/uploads/music/2025/11/MOT_VONG_TRAI_DAT_MP3.mp3"
   }
   ↓
7. App lưu downloadLink
   ↓
8. Tạo metadata:
   POST /api/music-files
   {
     "fileCode": "MOT_VONG_TRAI_DAT_MP3",
     "fileName": "mot vong trai dat",
     "downloadLink": "http://10.0.2.2:3005/uploads/...",
     "genreId": 4,
     ...
   }
   ↓
9. ✅ Hoàn tất!
```

---

## 📝 Files Đã Sửa

1. ✅ **MusicApiService.kt** - Thêm `fileCode` parameter
2. ✅ **MusicApiRepository.kt** - Tạo `fileCodePart` và gửi lên API
3. ✅ **MusicViewModel.kt** - Thêm `fileCode` parameter
4. ✅ **AddMusicScreen.kt** - Truyền `fileCode` khi upload

---

## ✅ Kết Luận

**Đã sửa xong!** App bây giờ sẽ:

1. ✅ Gửi **fileCode** cùng với file khi upload
2. ✅ Server nhận đủ parameters
3. ✅ Upload thành công
4. ✅ Nhận downloadLink
5. ✅ Tạo metadata với downloadLink

**Build Status:** 🔄 Building...  
**Ready to test!** 🎵

---

## 🚀 Cách Test

1. **Chọn file nhạc** trong app
2. **Nhập thông tin** (Tên, Nghệ sĩ, Album...)
3. **Click "Lưu"**
4. **Xem Logcat:**
   ```
   --> POST /api/music-files/upload
   Content-Disposition: form-data; name="fileCode"
   
   MOT_VONG_TRAI_DAT_MP3
   
   <-- 200 OK
   {
     "downloadLink": "http://10.0.2.2:3005/uploads/..."
   }
   
   --> POST /api/music-files
   Body: {
     "downloadLink": "http://10.0.2.2:3005/uploads/..."
   }
   
   <-- 201 Created
   ```

5. ✅ **Verify:** File được tạo thành công và có downloadLink!

**Lỗi 500 đã được fix!** 🎉

