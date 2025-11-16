# Tích hợp API Filter theo Genre

## Mô tả
Khi người dùng click vào các tab thể loại (Genre) trong màn hình thư viện (MainScreen), ứng dụng sẽ gọi API để lấy danh sách file nhạc theo thể loại đã chọn.

## API Endpoint
```
GET http://localhost:3005/api/music-files/filter/genre/{genreId}
```

### Parameters
- `genreId`: ID của thể loại (Integer) - lấy từ API Genre
- `page`: Trang (mặc định: 0)
- `size`: Số lượng item mỗi trang (mặc định: 100)
- `sort`: Sắp xếp (mặc định: createdAt,desc)

### Ví dụ
```
# Lấy tất cả file nhạc thuộc thể loại có ID = 1
GET http://localhost:3005/api/music-files/filter/genre/1

# Với phân trang
GET http://localhost:3005/api/music-files/filter/genre/1?page=0&size=10

# Với sort
GET http://localhost:3005/api/music-files/filter/genre/1?page=0&size=10&sort=fileName,asc
```

## Cách hoạt động

### 1. Dữ liệu Genre
- Khi ứng dụng khởi động, `GenreViewModel` tải tất cả thể loại từ API
- Mỗi genre có:
  - `id`: Mã thể loại (String) - vd: "pop", "rock", "jazz"
  - `apiId`: ID từ database API (Integer) - vd: 1, 2, 3
  - `name`: Tên hiển thị - vd: "Pop", "Rock", "Jazz"

### 2. Khi người dùng chọn tab
1. User click vào tab thể loại (ví dụ: "Pop")
2. MainScreen nhận event `onSelected` với Genre object
3. LaunchedEffect được trigger khi `selected` thay đổi
4. Kiểm tra:
   - Nếu chọn "Tất cả" → Gọi `loadMusicFiles()` để lấy tất cả
   - Nếu chọn thể loại cụ thể → Tìm `apiId` tương ứng và gọi `filterByGenre(apiId)`

### 3. Repository xử lý
`MusicApiRepository.filterByGenre(genreId)`:
- Gọi API với genre ID
- Nhận response (danh sách file nhạc)
- Chuyển đổi response thành Music objects
- Cập nhật StateFlow `_musicFiles` 
- UI tự động cập nhật khi StateFlow thay đổi

## Code Changes

### MainScreen.kt
```kotlin
// Lấy cả genres và genresWithId
val availableGenres by genreViewModel.genres.collectAsState()
val genresWithId by genreViewModel.genresWithId.collectAsState()

// Filter by genre when selected genre changes
LaunchedEffect(selected) {
    if (selected.id == "all") {
        // Load all music files
        musicViewModel.loadMusicFiles()
    } else {
        // Find the API genre ID and filter
        val genreWithId = genresWithId.find { it.genre.id == selected.id }
        genreWithId?.let {
            musicViewModel.filterByGenre(it.apiId)
        }
    }
}
```

### MusicApiRepository.kt
```kotlin
suspend fun filterByGenre(genreId: Int): ApiResult<List<Music>> {
    _isLoading.value = true
    _error.value = null

    val result = safeApiCall { apiService.filterByGenre(genreId) }

    when (result) {
        is ApiResult.Success -> {
            // Convert và store kết quả
            val musicFileWithIdList = result.data.content.map { /* convert */ }
            _musicFilesWithId.value = musicFileWithIdList
            _musicFiles.value = musicFileWithIdList.map { it.music }
            
            _isLoading.value = false
            return ApiResult.Success(_musicFiles.value)
        }
        // ... handle error
    }
}
```

### MusicViewModel.kt
```kotlin
fun filterByGenre(genreId: Int?) {
    _selectedGenreId.value = genreId

    if (genreId == null) {
        loadMusicFiles()
        return
    }

    viewModelScope.launch {
        repository.filterByGenre(genreId)
    }
}
```

## Testing

### 1. Khởi động API server
```bash
cd C:\Users\Admin\Desktop\app_music
start_music_api_server.bat
```

### 2. Test trên app
1. Mở ứng dụng
2. Vào màn hình Thư viện
3. Click vào tab "Pop" → Chỉ hiện file nhạc Pop
4. Click vào tab "Rock" → Chỉ hiện file nhạc Rock
5. Click vào tab "Tất cả" → Hiện tất cả file nhạc

### 3. Kiểm tra Network
- Dùng Logcat để xem API calls
- URL phải là: `http://10.0.2.2:3005/api/music-files/filter/genre/{genreId}`
- Response phải có cấu trúc PagedResponse<MusicFileResponse>

## Lưu ý
- API URL: `http://10.0.2.2:3005` (cho Android Emulator)
- Hoặc `http://localhost:3005` (cho máy thật qua ADB reverse)
- Đảm bảo server đang chạy trước khi test
- Nếu không có dữ liệu, kiểm tra database có file nhạc với genreId tương ứng không

## Troubleshooting

### Không lấy được dữ liệu
1. Kiểm tra server có đang chạy không
2. Kiểm tra URL đúng không (10.0.2.2 cho emulator)
3. Kiểm tra database có dữ liệu không
4. Xem Logcat để debug lỗi API

### Tab không đổi màu
- Kiểm tra FilterChips component có nhận `selected` đúng không

### Dữ liệu không cập nhật
- Kiểm tra Repository có update `_musicFiles` StateFlow không
- Kiểm tra ViewModel có collect StateFlow đúng không

