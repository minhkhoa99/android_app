# Hướng Dẫn Sử Dụng Music Library API

## 📋 Tổng Quan

Music Library API được tích hợp vào ứng dụng Android để quản lý các file nhạc. API chạy trên port **3005** và sử dụng json-server để mô phỏng REST API.

## 🏗️ Kiến Trúc

```
┌─────────────────┐
│  MainScreen.kt  │ ← UI Layer
└────────┬────────┘
         │
┌────────▼─────────┐
│ MusicViewModel   │ ← ViewModel Layer
└────────┬─────────┘
         │
┌────────▼──────────┐
│MusicApiRepository │ ← Repository Layer
└────────┬──────────┘
         │
┌────────▼──────────┐
│ MusicApiService   │ ← API Layer (Retrofit)
└────────┬──────────┘
         │
┌────────▼──────────┐
│   Port 3005       │ ← json-server
│  music_db.json    │
└───────────────────┘
```

## 📁 Files Đã Tạo

### 1. **API Layer**
- `MusicApiService.kt` - Định nghĩa các endpoint API
- `MusicFileModels.kt` - Request/Response models
- `ApiClient.kt` - Đã cập nhật thêm `musicService`

### 2. **Repository Layer**
- `MusicApiRepository.kt` - Quản lý dữ liệu và cache

### 3. **ViewModel Layer**
- `MusicViewModel.kt` - Quản lý UI state và business logic

### 4. **UI Layer**
- `MainScreen.kt` - Đã cập nhật để sử dụng ViewModel

### 5. **Server Files**
- `music_db.json` - Database với 8 bài hát mẫu
- `music_routes.json` - Cấu hình routes cho API
- `start_music_api_server.bat` - Script khởi động server

## 🚀 Cách Sử Dụng

### Bước 1: Khởi Động Server

```bash
# Mở terminal và chạy:
start_music_api_server.bat
```

Hoặc manual:
```bash
json-server --watch music_db.json --port 3005 --routes music_routes.json
```

### Bước 2: Kiểm Tra Server

Mở trình duyệt và truy cập:
```
http://localhost:3005/api/music-files
```

Nếu thấy danh sách nhạc → Server đã chạy OK!

### Bước 3: Build và Run App

```bash
.\gradlew installDebug
```

## 🔌 API Endpoints

### Get All Music Files
```
GET http://localhost:3005/api/music-files?page=0&size=20&sort=createdAt,desc
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "fileCode": "MF001",
      "fileName": "Bài hát mẫu.mp3",
      "genreId": 1,
      "genreName": "Pop Music",
      "artist": "Nghệ sĩ A",
      "album": "Album Demo",
      "duration": 240,
      "fileSize": 5242880,
      "releaseYear": 2020
    }
  ]
}
```

### Search Music Files
```
GET http://localhost:3005/api/music-files/search?keyword=rock
```

### Filter by Genre
```
GET http://localhost:3005/api/music-files/filter/genre/1
```

### Filter by Year
```
GET http://localhost:3005/api/music-files/filter/year/2020
```

### Create Music File
```
POST http://localhost:3005/api/music-files
Content-Type: application/json

{
  "fileCode": "MF009",
  "fileName": "New Song.mp3",
  "genreId": 1,
  "artist": "Artist Name",
  "album": "Album Name",
  "releaseYear": 2023,
  "duration": 180,
  "fileSize": 4000000
}
```

### Update Music File
```
PUT http://localhost:3005/api/music-files/1
Content-Type: application/json

{
  "fileCode": "MF001",
  "fileName": "Updated Song.mp3",
  "genreId": 1,
  "artist": "Updated Artist"
}
```

### Delete Music File
```
DELETE http://localhost:3005/api/music-files/1
```

## 📱 Sử Dụng Trong Android

### Load All Music Files
```kotlin
val viewModel: MusicViewModel = viewModel()

LaunchedEffect(Unit) {
    viewModel.loadMusicFiles()
}

val musicFiles by viewModel.musicFiles.collectAsState()
val isLoading by viewModel.isLoading.collectAsState()
val error by viewModel.error.collectAsState()
```

### Search Music
```kotlin
viewModel.searchMusicFiles("rock")
```

### Filter by Genre
```kotlin
viewModel.filterByGenre(genreId = 1)
```

### Create New Music
```kotlin
viewModel.createMusicFile(
    fileCode = "MF009",
    fileName = "New Song.mp3",
    genreId = 1,
    artist = "Artist Name",
    album = "Album Name",
    releaseYear = 2023
)
```

### Update Music
```kotlin
viewModel.updateMusicFile(
    id = 1,
    fileCode = "MF001",
    fileName = "Updated.mp3",
    genreId = 1
)
```

### Delete Music
```kotlin
viewModel.deleteMusicFile(id = 1, fileName = "Song.mp3")
```

## 🔍 Các Tính Năng Đã Được Tích Hợp

### ✅ Repository Layer
- Fetch all music files với pagination
- Search by keyword
- Filter by genre
- Filter by year
- Create/Update/Delete operations
- Local caching với StateFlow
- Error handling

### ✅ ViewModel Layer
- Manage UI state (loading, error, success)
- Business logic separation
- Reactive data với StateFlow
- Automatic data refresh after CUD operations

### ✅ UI Layer (MainScreen)
- Display music list from API
- Loading indicator
- Search functionality (local filter)
- Genre filter (local filter)
- Pull-to-refresh capability

## 🎯 Dữ Liệu Mẫu

`music_db.json` chứa 8 bài hát:
1. **MF001** - Bài hát mẫu.mp3 (Pop)
2. **MF002** - Rock Anthem.mp3 (Rock)
3. **MF003** - Smooth Jazz.wav (Jazz)
4. **MF004** - Hip Hop Beat.mp3 (Hip Hop)
5. **MF005** - Classical Symphony.flac (Pop)
6. **MF006** - EDM Party.mp3 (Rock)
7. **MF007** - Country Road.mp3 (Jazz)
8. **MF008** - Reggae Vibes.mp3 (Hip Hop)

## 🧪 Testing

### Test 1: Load Data
1. Khởi động server
2. Mở app
3. Vào tab "Thư viện"
4. Kiểm tra xem có hiển thị 8 bài hát không

### Test 2: Search
1. Nhập "rock" vào search box
2. Kiểm tra kết quả có hiển thị "Rock Anthem.mp3"

### Test 3: Filter by Genre
1. Click vào chip "Pop"
2. Kiểm tra chỉ hiển thị nhạc Pop

### Test 4: Create Music
1. Click nút FAB (+)
2. Nhập thông tin
3. Click "Lưu"
4. Kiểm tra bài hát mới xuất hiện trong danh sách

## 🔧 Troubleshooting

### Lỗi: "Không thể kết nối đến server"
- Kiểm tra server có đang chạy không: http://localhost:3005/api/music-files
- Kiểm tra firewall có chặn port 3005 không
- Restart server

### Lỗi: "Empty response body"
- Kiểm tra `music_db.json` có đúng format không
- Restart server

### App không hiển thị dữ liệu
- Kiểm tra Logcat xem có lỗi API không
- Kiểm tra emulator có kết nối internet không
- Kiểm tra BASE_URL trong `ApiClient.kt` (phải là `10.0.2.2` cho emulator)

## 📊 So Sánh Với Genre API

| Feature | Genre API (Port 3000) | Music API (Port 3005) |
|---------|----------------------|----------------------|
| Database | `db.json` | `music_db.json` |
| Routes | `routes.json` | `music_routes.json` |
| Repository | `GenreApiRepository` | `MusicApiRepository` |
| ViewModel | `GenreViewModel` | `MusicViewModel` |
| Screen | `GenreListScreen` | `MainScreen` |

## 📝 Next Steps

1. ✅ Tích hợp API vào MainScreen - **DONE**
2. ⏳ Tạo AddMusicScreen với API
3. ⏳ Tích hợp delete confirmation dialog
4. ⏳ Thêm pull-to-refresh
5. ⏳ Thêm pagination scroll
6. ⏳ Thêm file upload functionality

## 🎉 Kết Luận

API đã được tích hợp thành công vào màn Thư viện! Bây giờ bạn có thể:
- ✅ Load danh sách nhạc từ server
- ✅ Search và filter (local)
- ✅ Hiển thị loading state
- ✅ Xử lý lỗi
- ⏳ Thêm/Sửa/Xóa (cần tạo UI screens)

