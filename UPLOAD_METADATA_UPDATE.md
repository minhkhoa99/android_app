# Cập Nhật Metadata Từ Upload API

## Tóm tắt
Đã cập nhật luồng upload file nhạc để tự động lấy và lưu các trường metadata bổ sung từ API upload response vào database.

## Các thay đổi đã thực hiện

### 1. MusicApiRepository.kt
- **Cập nhật `UploadResult` data class** để bao gồm thêm các trường:
  - `fileSize: Long?` - Kích thước file (bytes)
  - `duration: Int?` - Thời lượng (giây)
  - `artist: String?` - Nghệ sĩ từ metadata
  - `album: String?` - Album từ metadata

- **Cập nhật `uploadMusicFile()` method** để:
  - Trích xuất các trường metadata từ `MusicFileResponse` của API
  - Trả về đầy đủ metadata trong `UploadResult`

### 2. AddMusicScreen.kt
- **Thêm state variables** để lưu metadata từ upload:
  ```kotlin
  var uploadedFileSize by remember { mutableStateOf<Long?>(null) }
  var uploadedDuration by remember { mutableStateOf<Int?>(null) }
  var uploadedArtist by remember { mutableStateOf<String?>(null) }
  var uploadedAlbum by remember { mutableStateOf<String?>(null) }
  ```

- **Auto-fill form fields** sau khi upload thành công:
  - Artist: Tự động điền nếu có từ metadata và trường đang trống
  - Album: Tự động điền nếu có từ metadata và trường đang trống
  - Duration: Tự động convert từ seconds sang format "MM:SS"
  - File Size: Tự động convert từ bytes sang MB

- **Sử dụng metadata từ upload** khi tạo music file:
  - Ưu tiên metadata từ upload API
  - Fallback sang giá trị nhập tay trong form nếu upload không có
  - Đảm bảo dữ liệu chính xác từ file thực tế

## Luồng hoạt động mới

### Khi user upload file:
1. **Upload file** → POST `/api/music-files/upload`
2. **Nhận response** với metadata:
   ```json
   {
     "fileCode": "a1b2c3d4...",
     "downloadLink": "/api/music-files/download/...",
     "fileSize": 5242880,
     "duration": 240,
     "artist": "Sơn Tùng MTP",
     "album": "Sky Tour"
   }
   ```
3. **Auto-fill form** với metadata từ server
4. **User xác nhận/chỉnh sửa** thông tin nếu cần
5. **Submit** → POST `/api/music-files` để lưu vào database

### Ưu điểm:
✅ Dữ liệu chính xác từ metadata của file thực tế  
✅ Giảm thời gian nhập liệu cho user  
✅ Tự động hóa việc lấy thông tin file  
✅ User vẫn có thể chỉnh sửa nếu cần  

## Các trường được lưu vào database

### Từ Upload API (metadata thực tế):
- `fileCode` - Mã file từ server
- `downloadLink` - Link download
- `fileSize` - Kích thước file (bytes)
- `duration` - Thời lượng (seconds)
- `artist` - Nghệ sĩ từ ID3 tags
- `album` - Album từ ID3 tags

### Từ Form (user input):
- `fileName` - Tên bài hát
- `genreId` - Thể loại
- `releaseYear` - Năm phát hành
- `description` - Mô tả

### Fallback logic:
```kotlin
val finalArtist = uploadedArtist?.takeIf { it.isNotBlank() } 
    ?: artist.ifBlank { null }

val finalAlbum = uploadedAlbum?.takeIf { it.isNotBlank() } 
    ?: album.ifBlank { null }

val finalFileSize = uploadedFileSize ?: /* parse từ form */
val finalDuration = uploadedDuration ?: /* parse từ form */
```

## Test checklist
- [ ] Upload file MP3 có metadata → Kiểm tra auto-fill artist, album
- [ ] Upload file không có metadata → Kiểm tra fallback sang form
- [ ] Xác nhận duration và fileSize chính xác
- [ ] Submit và kiểm tra dữ liệu trong database
- [ ] Kiểm tra hiển thị trên màn Library

## API Response Expected
```json
{
  "fileCode": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "filePath": "C:/uploads/music/a1b2c3d4-e5f6-7890-abcd-ef1234567890.mp3",
  "fileType": "mp3",
  "fileSize": 5242880,
  "duration": 240,
  "downloadLink": "/api/music-files/download/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "artist": "Sơn Tùng MTP",
  "album": "Sky Tour"
}
```

---
**Ngày cập nhật**: 2025-11-14

