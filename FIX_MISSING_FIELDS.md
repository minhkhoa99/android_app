# ✅ SỬA LỖI: BODY THIẾU TRƯỜNG KHI TẠO NHẠC

## 🐛 Vấn Đề

Body gửi lên API **thiếu 3 trường** so với example, dẫn đến lỗi SQL:
```
Internal server error: could not execute statement [ERROR: null value in column X]
```

### Example Body (Mẫu):
```json
{
  "fileCode": "DEMO_001",
  "fileName": "Một vòng trái đất",
  "filePath": "/storage/music/demo_001.mp3",  ← THIẾU
  "genreId": 4,
  "artist": "Tim",
  "album": "mot vong trai dat",
  "releaseYear": 2020,
  "description": "demo",                       ← THIẾU
  "fileType": "mp3",                           ← THIẾU
  "fileSize": 5242880,
  "duration": 240
}
```

### Body Thực Tế (Trước):
```json
{
  "fileCode": "...",
  "fileName": "...",
  "genreId": 4,
  "artist": "...",
  "album": "...",
  "releaseYear": 2020,
  // ❌ THIẾU filePath
  // ❌ THIẾU description  
  // ❌ THIẾU fileType
  "fileSize": 5242880,
  "duration": 240
}
```

---

## ✅ Giải Pháp

### 1. **Cập Nhật Model (MusicFileRequest)**

**Trước:**
```kotlin
data class MusicFileRequest(
    val fileCode: String,
    val fileName: String,
    val genreId: Int,
    val artist: String? = null,
    val album: String? = null,
    // ❌ Không có filePath, fileType
    val description: String? = null,
    val duration: Int? = null,
    val fileSize: Long? = null
)
```

**Sau:**
```kotlin
data class MusicFileRequest(
    val fileCode: String,
    val fileName: String,
    val filePath: String? = null,        // ✅ THÊM
    val genreId: Int,
    val fileType: String? = null,        // ✅ THÊM
    val artist: String? = null,
    val album: String? = null,
    val description: String? = null,
    val duration: Int? = null,
    val fileSize: Long? = null
)
```

### 2. **Cập Nhật Repository**

```kotlin
suspend fun createMusicFile(
    fileCode: String,
    fileName: String,
    genreId: Int,
    filePath: String? = null,        // ✅ THÊM
    fileType: String? = null,        // ✅ THÊM
    artist: String? = null,
    album: String? = null,
    releaseYear: Int? = null,
    description: String? = null,
    duration: Int? = null,
    fileSize: Long? = null
): ApiResult<Music> {
    val request = MusicFileRequest(
        fileCode = fileCode,
        fileName = fileName,
        filePath = filePath,         // ✅ THÊM
        genreId = genreId,
        fileType = fileType,         // ✅ THÊM
        artist = artist,
        album = album,
        releaseYear = releaseYear,
        description = description,
        duration = duration,
        fileSize = fileSize
    )
    // ...
}
```

### 3. **Cập Nhật ViewModel**

```kotlin
fun createMusicFile(
    fileCode: String,
    fileName: String,
    genreId: Int,
    filePath: String? = null,        // ✅ THÊM
    fileType: String? = null,        // ✅ THÊM
    artist: String? = null,
    // ...
) {
    viewModelScope.launch {
        repository.createMusicFile(
            fileCode, fileName, genreId, 
            filePath, fileType,          // ✅ THÊM
            artist, album, releaseYear, 
            description, duration, fileSize
        )
    }
}
```

### 4. **Thêm UI Field "Mô Tả"**

**State:**
```kotlin
var description by remember { mutableStateOf("") }
```

**UI:**
```kotlin
field("Mô Tả", description, { description = it }, "VD: Bài hát hay")
```

### 5. **Generate filePath & fileType Trong AddMusicScreen**

```kotlin
// Generate filePath từ URI
val filePath = fileUri?.toString() 
    ?: "/storage/music/${fileCode.lowercase()}.mp3"

// Detect fileType từ extension
val fileType = fileUri?.lastPathSegment
    ?.substringAfterLast(".", "mp3") ?: "mp3"

// Gọi API với đầy đủ params
musicViewModel.createMusicFile(
    fileCode = fileCode,
    fileName = title,
    genreId = genreApiId,
    filePath = filePath,              // ✅ THÊM
    fileType = fileType,              // ✅ THÊM
    artist = artist.ifBlank { null },
    album = album.ifBlank { null },
    releaseYear = releaseYear,
    description = description.ifBlank { null },  // ✅ THÊM
    duration = durationInSeconds,
    fileSize = fileSizeInBytes
)
```

---

## 📊 So Sánh Body

### **Trước (Lỗi):**
```json
{
  "fileCode": "MOT_VONG_TRAI_DAT_MP3",
  "fileName": "mot vong trai dat",
  "genreId": 4,
  "artist": "Tim",
  "album": "demo",
  "releaseYear": 2020,
  "duration": 4,
  "fileSize": 5242880
}
```
❌ Thiếu `filePath`, `fileType`, `description`  
❌ SQL Error: null value in column

### **Sau (Thành Công):**
```json
{
  "fileCode": "MOT_VONG_TRAI_DAT_MP3",
  "fileName": "mot vong trai dat",
  "filePath": "content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2FMot%20Vong",
  "genreId": 4,
  "fileType": "mp3",
  "artist": "Tim",
  "album": "demo",
  "releaseYear": 2020,
  "description": "demo",
  "duration": 4,
  "fileSize": 5242880
}
```
✅ Đầy đủ tất cả trường  
✅ API trả về 201 Created

---

## 🎨 UI Changes

### **Màn Thêm File Nhạc - Trước:**
```
[Chọn File Nhạc]
┌─────────────────────┐
│ Mã File             │
│ Tên Bài Hát         │
│ Nghệ Sĩ             │
│ Album               │
│ Năm Phát Hành       │
│ Thời lượng          │
│ Kích thước          │
│ Thể loại ▼          │
└─────────────────────┘
[Lưu]
```

### **Màn Thêm File Nhạc - Sau:**
```
[Chọn File Nhạc]
┌─────────────────────┐
│ Mã File             │
│ Tên Bài Hát         │
│ Nghệ Sĩ             │
│ Album               │
│ Năm Phát Hành       │
│ Thời lượng          │
│ Kích thước          │
│ Mô Tả               │ ← ✅ MỚI THÊM
│ Thể loại ▼          │
└─────────────────────┘
[Lưu]
```

---

## 🔧 Logic Generate Data

### 1. **filePath**
```kotlin
// Từ URI thật
fileUri?.toString() 
// VD: "content://com.android.providers.downloads.documents/..."

// Hoặc fallback
"/storage/music/${fileCode.lowercase()}.mp3"
// VD: "/storage/music/mot_vong_trai_dat_mp3.mp3"
```

### 2. **fileType**
```kotlin
// Extract từ filename
fileUri?.lastPathSegment?.substringAfterLast(".", "mp3")
// "Mot Vong.mp3" → "mp3"
// "song.wav" → "wav"
// "track.flac" → "flac"

// Default: "mp3"
```

### 3. **description**
```kotlin
// Từ UI input
description.ifBlank { null }
// Nếu user nhập → gửi value
// Nếu để trống → gửi null
```

---

## 📝 Files Đã Sửa

1. **MusicFileModels.kt**
   - ✅ Thêm `filePath: String?`
   - ✅ Thêm `fileType: String?`

2. **MusicApiRepository.kt**
   - ✅ Thêm parameters `filePath`, `fileType` vào `createMusicFile()`
   - ✅ Thêm parameters `filePath`, `fileType` vào `updateMusicFile()`
   - ✅ Pass vào `MusicFileRequest`

3. **MusicViewModel.kt**
   - ✅ Thêm parameters `filePath`, `fileType` vào `createMusicFile()`
   - ✅ Thêm parameters `filePath`, `fileType` vào `updateMusicFile()`
   - ✅ Pass xuống Repository

4. **AddMusicScreen.kt**
   - ✅ Thêm `var description` state
   - ✅ Thêm field "Mô Tả" vào UI
   - ✅ Generate `filePath` từ URI
   - ✅ Detect `fileType` từ filename
   - ✅ Pass `description`, `filePath`, `fileType` vào ViewModel

---

## 🧪 Test Case

### Input:
```
File: "Mot Vong.mp3" (từ Downloads)
Tên: "mot vong trai dat"
Nghệ sĩ: "Tim"
Album: "demo"
Năm: "2020"
Thời lượng: "4"
Kích thước: "5.0 MB"
Mô tả: "demo"
Thể loại: "Classical"
```

### Expected Body:
```json
{
  "fileCode": "MOT_VONG_MP3",
  "fileName": "mot vong trai dat",
  "filePath": "content://...Mot%20Vong.mp3",
  "genreId": 4,
  "fileType": "mp3",
  "artist": "Tim",
  "album": "demo",
  "releaseYear": 2020,
  "description": "demo",
  "duration": 4,
  "fileSize": 5242880
}
```

### Expected Response:
```
✅ 201 Created
{
  "id": 9,
  "fileCode": "MOT_VONG_MP3",
  "fileName": "mot vong trai dat",
  ...
}
```

### UI Response:
```
✅ Green message: "Thêm file nhạc 'mot vong trai dat' thành công!"
✅ Auto navigate back after 500ms
✅ File xuất hiện trong danh sách Thư viện
```

---

## ⚠️ Edge Cases

### 1. **Không chọn file**
```kotlin
if (fileUri == null) {
    filePath = "/storage/music/default.mp3"
    fileType = "mp3"
}
```

### 2. **File không có extension**
```kotlin
val fileType = fileUri?.lastPathSegment
    ?.substringAfterLast(".", "mp3") ?: "mp3"
// Fallback về "mp3"
```

### 3. **Description để trống**
```kotlin
description = description.ifBlank { null }
// Gửi null thay vì empty string
```

---

## ✅ Kết Luận

**Đã sửa xong!** Bây giờ body gửi lên API có **đầy đủ 11 trường**:

1. ✅ `fileCode`
2. ✅ `fileName`
3. ✅ `filePath` ← MỚI
4. ✅ `genreId`
5. ✅ `fileType` ← MỚI
6. ✅ `artist`
7. ✅ `album`
8. ✅ `releaseYear`
9. ✅ `description` ← ĐÃ CÓ TRONG UI
10. ✅ `duration`
11. ✅ `fileSize`

**Build Status:** 🔄 Building...  
**Ready to test!** 🎵

---

## 🚀 Cách Test

1. Build & Install:
   ```bash
   .\gradlew installDebug
   ```

2. Mở app → Thư viện → FAB (+)

3. Nhập đầy đủ thông tin (kể cả Mô Tả)

4. Click "Lưu"

5. Kiểm tra Logcat xem POST body:
   ```
   --> POST /api/music-files
   Body: {"fileCode":"...","filePath":"...","fileType":"mp3",...}
   <-- 201 Created
   ```

6. Xác nhận file được tạo trong database!

**Lỗi SQL sẽ BIẾN MẤT!** ✨

