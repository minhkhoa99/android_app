# Luồng Hoạt Động Màn Hình Thư Viện Nhạc (MainScreen)

Tài liệu này giải thích luồng dữ liệu, cách các thành phần tương tác, và danh sách các API được sử dụng cho màn hình Thư viện (`MainScreen.kt`).

## Tổng Quan

Màn hình này cũng tuân theo kiến trúc MVVM, đảm bảo sự phân tách rõ ràng giữa giao diện, logic nghiệp vụ và tầng dữ liệu. Luồng hoạt động chính là tải và hiển thị danh sách các bài hát từ server, đồng thời hỗ trợ các chức năng như tìm kiếm, lọc theo thể loại, sửa và xóa.

**Sơ đồ luồng chạy:**

```
[UI Layer]          [ViewModel Layer]     [Data Layer]
MainScreen.kt  <-->  MusicViewModel.kt  <-->  MusicApiRepository.kt  <-->  MusicApiService.kt (Retrofit)  <-->  [Web API Server]
     |                   |                       |                             |
     |-- (Hiển thị List) |                       |                             |-- (Định nghĩa Endpoint)
     |-- (Gửi sự kiện:   |-- (Xử lý logic,        |-- (Gọi API,                 |
     |   onClick,        |   cập nhật State)     |   quản lý cache)            |
     |   onSearch,       |                       |                             |
     |   onFilter)       '----------------------> '---------------------------> '-----------------------------> (Giao tiếp HTTP)
     |                                                                   
     '<------------------ '<---------------------- '<--------------------------- '<---------------------------- (Nhận dữ liệu)
      (Cập nhật UI)       (Cung cấp StateFlow)    (Cập nhật StateFlow)           (Trả về Response)       
```

## Danh Sách API Sử Dụng

Dưới đây là các API chính được màn hình thư viện và các chức năng liên quan sử dụng, định nghĩa trong `MusicApiService.kt`.

| Phương thức | Endpoint                               | Chức năng                                           |
| :----------- | :------------------------------------- | :-------------------------------------------------- |
| `GET`        | `/api/music-files`                     | Lấy danh sách tất cả các file nhạc (hỗ trợ phân trang). |
| `GET`        | `/api/music-files/search`              | Tìm kiếm file nhạc theo từ khóa (`keyword`).           |
| `GET`        | `/api/music-files/filter/genre/{id}`   | Lọc các file nhạc theo ID của thể loại.             |
| `GET`        | `/api/music-files/{id}`                | Lấy thông tin chi tiết của một file nhạc bằng ID.      |
| `PUT`        | `/api/music-files/{id}`                | Cập nhật thông tin của một file nhạc.                |
| `DELETE`     | `/api/music-files/{id}`                | Xóa một file nhạc khỏi hệ thống.                     |

*Lưu ý: Base URL cho các endpoint này là `http://10.0.2.2:3005`.*

## Chi Tiết Luồng Chạy

### 1. Tầng Giao Diện (UI Layer) - `MainScreen.kt`

Đây là Composable hiển thị toàn bộ giao diện của màn hình thư viện, bao gồm thanh tìm kiếm, bộ lọc thể loại, và danh sách các bài hát.

- **Khởi tạo và Lấy dữ liệu:**
  - `MainScreen` nhận vào `MusicViewModel` và `GenreViewModel`.
  - Nó sử dụng `LaunchedEffect(Unit)` để gọi `musicViewModel.loadMusicFiles()` một lần duy nhất khi màn hình được hiển thị lần đầu, kích hoạt quá trình tải dữ liệu.
  - Dữ liệu danh sách nhạc (`allMusicFiles`) và trạng thái loading (`isLoading`) được theo dõi thông qua `collectAsState()`.

- **Xử lý sự kiện người dùng:**
  - **Lọc theo thể loại:** Khi người dùng chọn một thể loại (`Genre`), `LaunchedEffect(selected)` được kích hoạt. Nó sẽ gọi `musicViewModel.filterByGenre(genreId)` nếu một thể loại cụ thể được chọn, hoặc `musicViewModel.loadMusicFiles()` nếu chọn "Tất cả".
  - **Tìm kiếm:** `SearchBar` cập nhật một biến `query`. Biến này được dùng để lọc danh sách `allMusicFiles` đã có sẵn ở phía UI để hiển thị kết quả ngay lập tức.
  - **Xem chi tiết/Sửa/Xóa:** Các sự kiện click trên mỗi `MusicCard` sẽ gọi đến các lambda function (`onItemClick`, `onEditClick`, `onDeleteClick`) được truyền từ ngoài vào, thường sẽ điều hướng đến màn hình khác hoặc hiển thị dialog xác nhận. Các hàm này truyền `apiId` của bài hát.

**Đoạn code minh họa trong `MainScreen.kt`:**
```kotlin
@Composable
fun MainScreen(
    musicViewModel: MusicViewModel = viewModel(),
    genreViewModel: GenreViewModel = viewModel(),
    // ...
) {
    // Lấy danh sách music từ ViewModel
    val allMusicFiles by musicViewModel.musicFiles.collectAsState()
    val isLoading by musicViewModel.isLoading.collectAsState()
    
    // Load data khi màn hình được hiển thị lần đầu
    LaunchedEffect(Unit) {
        musicViewModel.loadMusicFiles()
    }

    // Lọc theo thể loại khi `selected` thay đổi
    LaunchedEffect(selected) {
        if (selected.id == "all") {
            musicViewModel.loadMusicFiles()
        } else {
            // ... tìm genreId và gọi filterByGenre
            musicViewModel.filterByGenre(it.apiId)
        }
    }
    
    // ... Hiển thị MusicList, SearchBar, FilterChips
}
```

### 2. Tầng ViewModel (ViewModel Layer) - `MusicViewModel.kt`

`MusicViewModel` là nơi quản lý trạng thái và logic cho `MainScreen`.

- **Chức năng:**
  - Cung cấp các `StateFlow` chứa danh sách nhạc (`musicFiles`), trạng thái tải (`isLoading`), và lỗi (`error`) cho UI.
  - Cung cấp các phương thức để UI có thể gọi để thực hiện các hành động như `loadMusicFiles()`, `searchMusicFiles(keyword)`, `filterByGenre(genreId)`, và `deleteMusicFile(id)`.
  - Các phương thức này sẽ gọi xuống `MusicApiRepository` để thực hiện công việc.

- **Luồng hoạt động:**
  1.  Khi được gọi từ UI (ví dụ `loadMusicFiles()`), `ViewModel` sẽ khởi chạy một coroutine bằng `viewModelScope.launch`.
  2.  Bên trong coroutine, nó gọi phương thức tương ứng của `repository` (ví dụ `repository.fetchAllMusicFiles()`).
  3.  `ViewModel` không trực tiếp thay đổi dữ liệu, mà chỉ expose các `StateFlow` từ `repository` để UI lắng nghe.

**Đoạn code minh họa trong `MusicViewModel.kt`:**
```kotlin
class MusicViewModel : ViewModel() {

    private val repository = MusicApiRepository

    // Expose các state từ repository
    val musicFiles: StateFlow<List<Music>> = repository.musicFiles
    val isLoading: StateFlow<Boolean> = repository.isLoading
    // ...

    init {
        // Tải danh sách nhạc khi ViewModel được tạo
        loadMusicFiles()
    }

    fun loadMusicFiles() {
        viewModelScope.launch {
            repository.fetchAllMusicFiles()
        }
    }

    fun filterByGenre(genreId: Int?) {
        viewModelScope.launch {
            repository.filterByGenre(genreId)
        }
    }
    // ... các hàm khác
}
```

### 3. Tầng Dữ Liệu (Data Layer) - `MusicApiRepository.kt`

`MusicApiRepository` hoạt động như một cầu nối giữa `ViewModel` và `MusicApiService`.

- **Chức năng:**
  - Gọi các hàm từ `apiService` để thực hiện yêu cầu mạng thực sự.
  - Quản lý trạng thái (`_isLoading`, `_error`) và một cache local cho danh sách nhạc (`_musicFiles`, `_musicFilesWithId`).
  - Xử lý `ApiResult` trả về từ `safeApiCall` và cập nhật các `StateFlow` tương ứng.
  - Chuyển đổi dữ liệu từ `MusicFileResponse` (mô hình của API) sang `Music` (mô hình của app).

- **Luồng hoạt động (ví dụ với `fetchAllMusicFiles`):**
  1.  `_isLoading.value` được set thành `true`.
  2.  Gọi `apiService.getAllMusicFiles()` bên trong `safeApiCall`.
  3.  Khi nhận được `ApiResult.Success`, nó sẽ:
      a. Duyệt qua danh sách `response.content`.
      b. Chuyển đổi mỗi `MusicFileResponse` thành `MusicFileWithId` và `Music`.
      c. Cập nhật `_musicFilesWithId` và `_musicFiles` với dữ liệu mới.
      d. Set `_isLoading.value` thành `false`.
  4.  Nếu nhận được `ApiResult.Error`, nó cập nhật `_error.value` và set `_isLoading.value` thành `false`.

**Đoạn code minh họa trong `MusicApiRepository.kt`:**
```kotlin
object MusicApiRepository {

    private val apiService = ApiClient.musicService

    private val _musicFiles = MutableStateFlow<List<Music>>(emptyList())
    val musicFiles: StateFlow<List<Music>> = _musicFiles.asStateFlow()
    // ...

    suspend fun fetchAllMusicFiles(): ApiResult<List<Music>> {
        _isLoading.value = true
        val result = safeApiCall { apiService.getAllMusicFiles(page = 0, size = 100) }

        when (result) {
            is ApiResult.Success -> {
                // Chuyển đổi response sang model của app
                val musicList = result.data.content.map { it.toMusic() }
                _musicFiles.value = musicList // Cập nhật cache
                _isLoading.value = false
                return ApiResult.Success(_musicFiles.value)
            }
            // ... xử lý Error và Loading
        }
    }
}
```

### 4. Tầng Dịch Vụ API (API Service Layer) - `MusicApiService.kt`

Đây là `interface` của Retrofit, định nghĩa "bản hợp đồng" giữa ứng dụng và web API.

- **Chức năng:**
  - Cung cấp một danh sách các hàm `suspend` tương ứng với mỗi endpoint của API.
  - Retrofit sẽ tự động tạo ra một implementation cho interface này, xử lý việc tạo và gửi các yêu cầu HTTP cũng như phân tích (parse) JSON response.
- **Ví dụ về định nghĩa endpoint:**
  - `@GET("music-files")`: Định nghĩa một yêu cầu `GET` tới đường dẫn `/api/music-files`.
  - `@Query("keyword")`: Định nghĩa một tham số truy vấn (query parameter), ví dụ: `/search?keyword=abc`.
  - `@Path("id")`: Định nghĩa một tham số đường dẫn, ví dụ: `/music-files/123`.

Việc tách biệt này giúp mã nguồn rất dễ đọc, bảo trì và kiểm thử. Nếu API thay đổi, chúng ta chỉ cần cập nhật tại `MusicApiService.kt` và `MusicApiRepository.kt`.