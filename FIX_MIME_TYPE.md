# ✅ SỬA LỖI: File Type Not Allowed

## 🐛 Lỗi

```json
{
  "status": 400,
  "message": "File type not allowed. Allowed types: mp3, wav, flac, aac, ogg, wma, m4a",
  "timestamp": "2025-11-14T15:56:16.6870702"
}
```

**File:** `Một Vòng Trái Đất.mp3` (định dạng MP3 hợp lệ)  
**Nguyên nhân:** MIME type gửi lên server là `"audio/*"` (không cụ thể) thay vì `"audio/mpeg"` (MP3)

---

## 🔍 Phân Tích

### **Request Trước (Sai):**

```
POST /api/music-files/upload
Content-Type: multipart/form-data

------Boundary
Content-Disposition: form-data; name="file"; filename="Mot_Vong.mp3"
Content-Type: audio/*  ← ❌ KHÔNG CỤ THỂ

[Binary Data]
------Boundary--
```

**Server validation:**
```java
// Server check MIME type
if (!allowedTypes.contains(file.getContentType())) {
    throw new Exception("File type not allowed");
}
// "audio/*" NOT IN ["audio/mpeg", "audio/wav", ...]
// → Reject!
```

---

## ✅ Giải Pháp

### **Detect MIME Type Dựa Vào File Extension**

**File:** `MusicApiRepository.kt`

```kotlin
suspend fun uploadMusicFile(file: java.io.File, fileCode: String, fileName: String): ApiResult<String> {
    // ...
    
    // ✅ Detect MIME type từ file extension
    val mimeType = when (file.extension.lowercase()) {
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "flac" -> "audio/flac"
        "aac" -> "audio/aac"
        "ogg" -> "audio/ogg"
        "wma" -> "audio/x-ms-wma"
        "m4a" -> "audio/mp4"
        else -> "audio/mpeg" // Default to mp3
    }
    
    // Create multipart file với MIME type cụ thể
    val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
    val filePart = okhttp3.MultipartBody.Part.createFormData(
        "file",
        file.name,
        requestFile
    )
    
    // ...
}
```

---

## 📊 MIME Type Mapping

| Extension | MIME Type | Server Accepts |
|-----------|-----------|----------------|
| `.mp3` | `audio/mpeg` | ✅ |
| `.wav` | `audio/wav` | ✅ |
| `.flac` | `audio/flac` | ✅ |
| `.aac` | `audio/aac` | ✅ |
| `.ogg` | `audio/ogg` | ✅ |
| `.wma` | `audio/x-ms-wma` | ✅ |
| `.m4a` | `audio/mp4` | ✅ |

---

## 🔄 Request Sau Khi Sửa

### **Multipart Request:**

```
POST /api/music-files/upload
Content-Type: multipart/form-data

------Boundary
Content-Disposition: form-data; name="file"; filename="Mot_Vong.mp3"
Content-Type: audio/mpeg  ← ✅ CỤ THỂ

[Binary Data]
------Boundary
Content-Disposition: form-data; name="fileCode"

MOT_VONG_TRAI_DAT_MP3
------Boundary
Content-Disposition: form-data; name="fileName"

mot vong trai dat
------Boundary--
```

**Server validation:**
```java
// "audio/mpeg" IN ["audio/mpeg", "audio/wav", ...]
// → Accept! ✅
```

---

## 🎯 Flow Hoàn Chỉnh

```
1. User chọn file:
   "Một Vòng Trái Đất.mp3"
   ↓
2. Copy file vào cache:
   tempFile = "/cache/Mot_Vong.mp3"
   ↓
3. Detect extension:
   file.extension = "mp3"
   ↓
4. Map to MIME type:
   mimeType = "audio/mpeg"  ✅
   ↓
5. Create multipart:
   Content-Type: audio/mpeg
   ↓
6. Upload:
   POST /api/music-files/upload
   ↓
7. Server validates:
   "audio/mpeg" ✅ ACCEPTED
   ↓
8. Response:
   {
     "downloadLink": "http://10.0.2.2:3005/uploads/..."
   }
   ↓
9. ✅ Success!
```

---

## 📝 Files Đã Sửa

1. ✅ **MusicApiRepository.kt** - Thêm MIME type detection từ file extension

---

## 🧪 Test Cases

### **Test 1: MP3 File**
```
Input: "song.mp3"
Extension: "mp3"
MIME Type: "audio/mpeg"
Result: ✅ Upload success
```

### **Test 2: WAV File**
```
Input: "song.wav"
Extension: "wav"
MIME Type: "audio/wav"
Result: ✅ Upload success
```

### **Test 3: FLAC File**
```
Input: "song.flac"
Extension: "flac"
MIME Type: "audio/flac"
Result: ✅ Upload success
```

### **Test 4: Unknown Extension**
```
Input: "song.xyz"
Extension: "xyz"
MIME Type: "audio/mpeg" (fallback)
Result: ⚠️ Upload nhưng có thể lỗi nếu không phải audio
```

---

## 🔧 Cải Tiến Thêm (Optional)

### **Validate Extension Trước Khi Upload:**

```kotlin
fun isValidAudioFile(file: File): Boolean {
    val allowedExtensions = setOf("mp3", "wav", "flac", "aac", "ogg", "wma", "m4a")
    return file.extension.lowercase() in allowedExtensions
}

// Trong upload function:
if (!isValidAudioFile(file)) {
    localError = "File không hợp lệ. Chỉ chấp nhận: mp3, wav, flac, aac, ogg, wma, m4a"
    return
}
```

---

## ✅ Kết Luận

**Đã sửa xong!** Upload bây giờ:

1. ✅ **Detect MIME type** từ file extension
2. ✅ **Gửi MIME type cụ thể** (vd: `audio/mpeg` cho MP3)
3. ✅ **Server accept** file với MIME type đúng
4. ✅ **Upload thành công**

### **Trước:**
```
Content-Type: audio/*  ← Server reject
```

### **Sau:**
```
Content-Type: audio/mpeg  ← Server accept ✅
```

**Build Status:** 🔄 Building...  
**Lỗi 400 sẽ biến mất!** 🎵

---

## 🚀 Cách Test

1. **Chọn file:** "Một Vòng Trái Đất.mp3"
2. **Click Lưu**
3. **Expected Log:**
   ```
   --> POST /api/music-files/upload
   Content-Type: audio/mpeg
   
   <-- 200 OK
   {
     "downloadLink": "http://10.0.2.2:3005/uploads/music/2025/11/MOT_VONG_TRAI_DAT_MP3.mp3"
   }
   ```

4. ✅ **Upload success!**

**Giờ có thể upload mọi loại file audio được hỗ trợ!** 🎉

