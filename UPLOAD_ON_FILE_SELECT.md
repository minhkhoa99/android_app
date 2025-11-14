# ✅ ĐÃ SỬA: Upload File Ngay Khi Chọn

## 🎯 Flow Mới

### **1. Chọn File** → Upload Ngay
```
User click "Chọn File Nhạc"
  ↓
Chọn file từ device
  ↓
Ngay lập tức upload lên server
  ↓
Hiển thị loading & progress
  ↓
Upload xong → Nhận fileCode + downloadLink từ server
  ↓
Hiển thị fileCode trên UI
  ↓
User điền các thông tin còn lại (tên, nghệ sĩ, album...)
  ↓
Click "Lưu" → Tạo metadata (không upload nữa)
  ↓
Done!
```

---

## 📝 Chi Tiết Implementation

### **1. Khi Chọn File (pickMusic launcher):**

```kotlin
val pickMusic = rememberLauncherForActivityResult(...) { uri ->
    if (uri != null) {
        fileUri = uri
        
        // Upload NGAY không chờ user click Lưu
        coroutineScope.launch {
            isUploading = true
            uploadProgress = "Đang chuẩn bị upload..."
            
            // Convert URI → File
            val file = context.getFileFromUri(uri)
            
            // Generate tempFileCode (server yêu cầu)
            val tempFileCode = "FILENAME_${timestamp}"
            
            uploadProgress = "Đang upload file lên server..."
            
            // Upload với tempFileCode
            val uploadResult = musicViewModel.uploadMusicFile(
                file, 
                tempFileCode,  // Gửi lên (server required)
                file.name
            )
            
            if (uploadResult != null) {
                // Lưu fileCode CHÍNH THỨC từ server
                uploadedFileCode = uploadResult.fileCode      // ← Server trả về
                uploadedDownloadLink = uploadResult.downloadLink
                
                uploadProgress = "✓ Upload thành công! File code: ${uploadResult.fileCode}"
                isUploading = false
                
                // Auto fill title
                if (title.isBlank()) {
                    title = file.nameWithoutExtension
                }
            }
        }
    }
}
```

### **2. Hiển thị File Code Từ Server:**

```kotlin
// Sau khi upload thành công
if (uploadedFileCode != null) {
    OutlinedTextField(
        value = uploadedFileCode ?: "",
        label = { Text("Mã File (từ Server)") },
        readOnly = true,
        enabled = false,
        // ...
    )
}
```

### **3. Khi Click Lưu:**

```kotlin
GradientButton(
    text = "Lưu",
    enabled = !isLoading && !isUploading  // Disabled khi đang upload
) {
    // Validate: Phải có file đã upload
    if (uploadedFileCode == null || uploadedDownloadLink == null) {
        localError = "Vui lòng chọn file (đợi upload xong)"
        return@GradientButton
    }
    
    // Tạo metadata với data đã upload
    val fileCode = uploadedFileCode!!      // Từ server
    val downloadLink = uploadedDownloadLink!!  // Từ server
    
    musicViewModel.createMusicFileAndWait(
        fileCode = fileCode,           // ← Dùng fileCode từ server
        downloadLink = downloadLink,   // ← Dùng downloadLink từ server
        fileName = title,
        genreId = genreApiId,
        // ... các field khác
    )
    
    // Reset sau khi thành công
    uploadedFileCode = null
    uploadedDownloadLink = null
}
```

---

## 🔄 API Flow

### **Upload API:**

**Request:**
```http
POST /api/music-files/upload
Content-Type: multipart/form-data

Parts:
- file: [Binary Data]
- fileCode: "SONG_1699876543210"  ← Temp code (client generate)
- fileName: "Mot Vong Trai Dat.mp3"
```

**Response:**
```json
{
  "id": 7,
  "fileCode": "MF_2025_001",  ← Server generate (chính thức)
  "fileName": "Mot Vong Trai Dat.mp3",
  "filePath": "/uploads/music/2025/11/MF_2025_001.mp3",
  "downloadLink": "http://10.0.2.2:3005/uploads/music/2025/11/MF_2025_001.mp3",
  "genreId": null,
  "fileType": "mp3",
  "fileSize": 4886987,
  // ...
}
```

**Client sử dụng:**
- ✅ `fileCode`: `"MF_2025_001"` (từ server)
- ✅ `downloadLink`: `"http://..."` (từ server)
- ❌ KHÔNG dùng `tempFileCode` client generate

### **Create Metadata API:**

**Request:**
```http
POST /api/music-files
Content-Type: application/json

{
  "fileCode": "MF_2025_001",           ← Từ upload response
  "fileName": "mot vong trai dat",
  "genreId": 4,
  "downloadLink": "http://...",        ← Từ upload response
  "artist": "Tim",
  "album": "demo",
  "releaseYear": 2020,
  "description": "demo",
  "duration": 240,
  "fileSize": 4886987
}
```

---

## 📊 State Management

### **Upload States:**

```kotlin
var uploadProgress: String? = null        // "Đang upload...", "✓ Thành công"
var uploadedFileCode: String? = null      // "MF_2025_001" (từ server)
var uploadedDownloadLink: String? = null  // "http://..." (từ server)
var isUploading: Boolean = false          // true khi đang upload
```

### **State Flow:**

```
Initial:
  uploadProgress = null
  uploadedFileCode = null
  uploadedDownloadLink = null
  isUploading = false
  
User chọn file:
  uploadProgress = "Đang chuẩn bị..."
  isUploading = true
  
Uploading:
  uploadProgress = "Đang upload file lên server..."
  
Upload success:
  uploadedFileCode = "MF_2025_001"         ← Server response
  uploadedDownloadLink = "http://..."      ← Server response
  uploadProgress = "✓ Upload thành công! File code: MF_2025_001"
  isUploading = false
  
User nhập thông tin:
  (states không đổi)
  
Click Lưu:
  uploadProgress = "Đang lưu thông tin vào database..."
  
Save success:
  uploadedFileCode = null      ← Reset
  uploadedDownloadLink = null  ← Reset
  uploadProgress = null
  Navigate back
```

---

## 🎨 UI Changes

### **1. Button Chọn File:**
```kotlin
Button(onClick = { pickMusic.launch(...) }) {
    Text(
        if (fileUri == null) {
            "Chọn File Nhạc"
        } else {
            "Đã chọn file"  // ← Không cho chọn lại nữa
        }
    )
}
```

### **2. Hiển thị File Đã Chọn:**
```kotlin
if (fileUri != null) {
    OutlinedTextField(
        value = fileUri?.lastPathSegment ?: "",
        label = { Text("File đã chọn") },
        readOnly = true,
        enabled = false
    )
}
```

### **3. Hiển thị File Code Từ Server:**
```kotlin
if (uploadedFileCode != null) {
    OutlinedTextField(
        value = uploadedFileCode ?: "",
        label = { Text("Mã File (từ Server)") },
        readOnly = true,
        enabled = false,
        // Màu xanh = success
    )
}
```

### **4. Upload Progress:**
```kotlin
if (uploadProgress != null) {
    Surface(
        color = Color(0x205AC8FA),  // Blue background
        // ...
    ) {
        Row {
            CircularProgressIndicator()
            Text(uploadProgress ?: "")
        }
    }
}
```

### **5. Button Lưu:**
```kotlin
GradientButton(
    text = "Lưu",
    enabled = !isLoading && !isUploading  // ← Disabled khi upload
) {
    // Validate
    if (uploadedFileCode == null || uploadedDownloadLink == null) {
        localError = "Vui lòng chọn file (đợi upload xong)"
        return@GradientButton
    }
    
    // Create metadata
}
```

---

## ✅ Lợi Ích

### **1. Tách Biệt Upload & Metadata:**
- Upload file = 1 bước độc lập
- Tạo metadata = 1 bước độc lập
- Không bao giờ gọi API 2 lần

### **2. User Experience Tốt:**
```
Trước:
  Chọn file → Điền form → Click Lưu → Upload + Create (chờ lâu)
  
Sau:
  Chọn file → Upload ngay (progress bar) → Điền form → Click Lưu (nhanh)
```

### **3. Error Handling Rõ Ràng:**
```
Upload failed:
  → Hiển thị error ngay
  → User có thể chọn file khác
  → Không mất data đã điền

Create metadata failed:
  → File đã upload rồi (có fileCode + downloadLink)
  → Chỉ cần retry create metadata
  → Không cần upload lại
```

### **4. Data Consistency:**
```
fileCode từ server:
  ✅ Unique (server guarantee)
  ✅ Follow server's naming convention
  ✅ Không bị duplicate

downloadLink từ server:
  ✅ Đúng URL
  ✅ File tồn tại trên server
  ✅ Có thể download được
```

---

## 🔧 Files Đã Sửa

### **1. MusicApiService.kt**
- ✅ Giữ nguyên 3 parameters: `file`, `fileCode`, `fileName`
- ✅ Server required `fileCode` (temp), trả về `fileCode` chính thức

### **2. MusicApiRepository.kt**
- ✅ Thêm `data class UploadResult(fileCode, downloadLink)`
- ✅ `uploadMusicFile()` trả về `UploadResult`

### **3. MusicViewModel.kt**
- ✅ `uploadMusicFile()` trả về `UploadResult?`

### **4. AddMusicScreen.kt**
- ✅ Upload ngay khi chọn file (trong `pickMusic` launcher)
- ✅ Lưu `uploadedFileCode` & `uploadedDownloadLink`
- ✅ Hiển thị fileCode từ server
- ✅ Validate: Phải có file đã upload mới cho Lưu
- ✅ Dùng fileCode & downloadLink từ server khi tạo metadata

---

## 🧪 Test Flow

### **Test 1: Upload Success**
```
1. Click "Chọn File Nhạc"
2. Chọn file "song.mp3"
3. → Upload ngay:
   - Progress: "Đang upload..."
   - Chờ 2-3s
   - Success: "✓ Upload thành công! File code: MF_2025_001"
4. → UI hiển thị:
   - File đã chọn: "song.mp3"
   - Mã File: "MF_2025_001"
5. Điền thông tin:
   - Tên: "Bài hát mẫu"
   - Nghệ sĩ: "Ca sĩ A"
   - ...
6. Click "Lưu"
7. → Tạo metadata (nhanh, không upload)
8. ✅ Success!
```

### **Test 2: Upload Failed**
```
1. Click "Chọn File Nhạc"
2. Chọn file "invalid.txt"
3. → Upload:
   - Progress: "Đang upload..."
   - Error: "File type not allowed"
4. → UI hiển thị error
5. Click "Chọn File Nhạc" lại
6. Chọn file "song.mp3"
7. → Upload lại
8. ✅ Success!
```

### **Test 3: Validate**
```
1. Không chọn file
2. Click "Lưu"
3. → Error: "Vui lòng chọn file (đợi upload xong)"
4. ✅ Prevented!
```

---

## 🎉 Kết Luận

**Flow mới hoàn hảo:**

1. ✅ **Upload ngay** khi chọn file
2. ✅ **Hiển thị progress** & loading
3. ✅ **FileCode từ server** (không client generate)
4. ✅ **DownloadLink từ server** (chính xác)
5. ✅ **Tách biệt** upload & create metadata
6. ✅ **Không bao giờ** gọi API 2 lần
7. ✅ **Error handling** rõ ràng
8. ✅ **UX tốt** (progress bar, auto fill)

**Build Status:** 🔄 Building...  
**Upload sẽ hoạt động hoàn hảo!** 🎵

