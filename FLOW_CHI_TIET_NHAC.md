# Luồng Hoạt Động Màn Hình Chi Tiết File Nhạc (MusicDetailScreen)

Tài liệu này giải thích luồng dữ liệu, cách các thành phần tương tác, và API được sử dụng cho màn hình Chi tiết File nhạc (`MusicDetailScreen.kt`).

## Tổng Quan

Màn hình chi tiết có nhiệm vụ hiển thị đầy đủ thông tin của một file nhạc cụ thể được chọn từ màn hình danh sách. Luồng hoạt động bắt đầu khi người dùng điều hướng đến màn hình này với một ID file nhạc, sau đó ứng dụng sẽ dùng ID này để truy vấn thông tin chi tiết từ API và hiển thị lên giao diện.

**Sơ đồ luồng chạy:**

```
[Navigation]  ---- (pass musicId) ---> [UI Layer]             [ViewModel Layer]         [Data Layer]
                                       MusicDetailScreen.kt  ---> MusicViewModel.kt  ---> MusicApiRepository.kt  ---> MusicApiService.kt  ---> [Web API]
                                            |                        |                         |                          |
(Hiển thị UI) <---- (Cung cấp State) <---- (Cập nhật State) <---- (Trả về Result) <---- (Thực thi API call) <---- (GET /api/music-files/{id})
```

## Danh Sách API Sử Dụng

Màn hình này chủ yếu sử dụng một endpoint duy nhất để lấy dữ liệu.

| Phương thức | Endpoint                  | Chức năng                                    |
| :----------- | :------------------------ | :------------------------------------------- |
| `GET`        | `/api/music-files/{id}`   | Lấy thông tin chi tiết của một file nhạc theo ID. |

*Lưu ý: Base URL cho endpoint này là `http://10.0.2.2:3005`.*

## Chi Tiết Luồng Chạy

### 1. Tầng Điều Hướng (Navigation) - `Routes.kt`

- **Định nghĩa Route:** Luồng hoạt động bắt đầu từ việc điều hướng. Trong `Routes.kt`, route cho màn hình chi tiết được định nghĩa với một tham số `id`.
  ```kotlin
  object Routes {
      const val Detail = "detail/{id}"
      // ...
      fun detail(id: String) = "detail/$id"
  }
  ```
- **Truyền Tham số:** Khi người dùng nhấn vào một bài hát ở màn hình danh sách (`MainScreen`), hành động `onItemClick(apiId)` được gọi, và `NavController` sẽ điều hướng đến route `detail/123` (với `123` là `apiId` của bài hát).

### 2. Tầng Giao Diện (UI Layer) - `MusicDetailScreen.kt`

Đây là Composable chịu trách nhiệm hiển thị thông tin chi tiết.

- **Nhận Tham số:** `MusicDetailScreen` nhận `musicId` (dưới dạng `String`) từ `NavHost` của Jetpack Compose Navigation.
- **Kích hoạt tải dữ liệu:**
  - Nó sử dụng `LaunchedEffect(musicId)` để thực thi một khối lệnh bất đồng bộ mỗi khi `musicId` thay đổi.
  - Bên trong `LaunchedEffect`, nó chuyển đổi `musicId` sang `Int` và gọi phương thức `musicViewModel.loadMusicFileById(id)`. Đây là một hàm `suspend`, nên nó sẽ chờ cho đến khi có kết quả từ API.
  - Kết quả trả về (một đối tượng `MusicDetail?`) được gán vào biến trạng thái `musicDetail` của màn hình.
- **Hiển thị dữ liệu:**
  - Giao diện sẽ hiển thị các trạng thái khác nhau dựa trên các biến:
    - `isLoading`: Hiển thị `CircularProgressIndicator`.
    - `error != null`: Hiển thị thông báo lỗi.
    - `musicDetail != null`: Hiển thị thông tin chi tiết của file nhạc.
  - Để hiển thị tên thể loại, nó sử dụng `genreViewModel` để tìm tên (`name`) tương ứng với `genreId` nhận được từ `musicDetail`.

**Đoạn code minh họa trong `MusicDetailScreen.kt`:**
```kotlin
@Composable
fun MusicDetailScreen(
    musicId: String = "1",
    musicViewModel: MusicViewModel = viewModel(),
    // ...
) {
    var musicDetail by remember { mutableStateOf<MusicDetail?>(null) }
    val isLoading by musicViewModel.isLoading.collectAsState()

    // Tải dữ liệu chi tiết khi musicId thay đổi
    LaunchedEffect(musicId) {
        val id = musicId.toIntOrNull()
        if (id != null) {
            musicDetail = musicViewModel.loadMusicFileById(id)
        }
    }

    // ... Giao diện hiển thị dựa trên isLoading, error, và musicDetail
}
```

### 3. Tầng ViewModel (ViewModel Layer) - `MusicViewModel.kt`

`MusicViewModel` được tái sử dụng từ màn hình danh sách, nhưng ở đây chúng ta tập trung vào phương thức lấy dữ liệu chi tiết.

- **Chức năng:**
  - Cung cấp phương thức `loadMusicFileById(id)` để UI có thể yêu cầu tải dữ liệu.
  - Quản lý trạng thái của đối tượng đang được chọn (`_selectedMusicDetail`).

- **Luồng hoạt động:**
  1.  Hàm `suspend fun loadMusicFileById(id: Int)` được gọi từ `MusicDetailScreen`.
  2.  Nó gọi xuống phương thức `repository.getMusicDetailById(id)`.
  3.  Khi nhận được kết quả (`ApiResult`) từ repository:
      - Nếu là `ApiResult.Success`, nó cập nhật `_selectedMusicDetail.value` với dữ liệu nhận được và trả về đối tượng `MusicDetail`.
      - Nếu là `ApiResult.Error`, nó set `_selectedMusicDetail.value` thành `null` và trả về `null`.
      - `MusicDetailScreen` sẽ nhận được kết quả này và cập nhật UI tương ứng.

**Đoạn code minh họa trong `MusicViewModel.kt`:**
```kotlin
class MusicViewModel : ViewModel() {
    // ...
    private val _selectedMusicDetail = MutableStateFlow<MusicDetail?>(null)
    val selectedMusicDetail: StateFlow<MusicDetail?> = _selectedMusicDetail.asStateFlow()

    suspend fun loadMusicFileById(id: Int): MusicDetail? {
        val result = repository.getMusicDetailById(id)
        return when (result) {
            is ApiResult.Success -> {
                _selectedMusicDetail.value = result.data
                result.data
            }
            is ApiResult.Error -> {
                _selectedMusicDetail.value = null
                null
            }
            is ApiResult.Loading -> null
        }
    }
}
```

### 4. Tầng Dữ Liệu (Data Layer) - `MusicApiRepository.kt`

`MusicApiRepository` là nơi thực hiện cuộc gọi API thực sự.

- **Chức năng:**
  - Trừu tượng hóa việc gọi API. `ViewModel` không cần biết chi tiết về Retrofit hay endpoint.
  - Chuyển đổi `MusicFileResponse` (mô hình dữ liệu của API) thành `MusicDetail` (mô hình dữ liệu của ứng dụng).

- **Luồng hoạt động:**
  1.  Hàm `suspend fun getMusicDetailById(id: Int)` được gọi từ `ViewModel`.
  2.  Nó set `_isLoading.value = true`.
  3.  Nó gọi `safeApiCall { apiService.getMusicFileById(id) }`.
  4.  Khi nhận được `ApiResult.Success`:
      - Nó lấy `result.data` (là một `MusicFileResponse`).
      - Gọi hàm extension `toMusicDetail()` để chuyển đổi nó thành đối tượng `MusicDetail`.
      - Trả về `ApiResult.Success` chứa đối tượng `MusicDetail` này.
  5.  Nếu có lỗi, nó cập nhật `_error.value` và trả về `ApiResult.Error`.
  6.  Cuối cùng, nó set `_isLoading.value = false`.

**Đoạn code minh họa trong `MusicApiRepository.kt`:**
```kotlin
object MusicApiRepository {
    // ...
    suspend fun getMusicDetailById(id: Int): ApiResult<MusicDetail> {
        _isLoading.value = true
        val result = safeApiCall { apiService.getMusicFileById(id) }
        _isLoading.value = false

        return when (result) {
            is ApiResult.Success -> {
                // Chuyển đổi response sang model chi tiết của app
                val musicDetail = result.data.toMusicDetail()
                ApiResult.Success(musicDetail)
            }
            is ApiResult.Error -> {
                _error.value = result.message
                result
            }
            is ApiResult.Loading -> result
        }
    }
}
```

### 5. Tầng Dịch Vụ API (API Service Layer) - `MusicApiService.kt`

Interface của Retrofit này định nghĩa endpoint để lấy một file nhạc.

- **Định nghĩa Endpoint:**
  ```kotlin
  interface MusicApiService {
      @GET("music-files/{id}")
      suspend fun getMusicFileById(@Path("id") id: Int): Response<MusicFileResponse>
  }
  ```
- `@GET("music-files/{id}")` chỉ định đây là một yêu cầu `GET`.
- `@Path("id")` thay thế `{id}` trong URL bằng giá trị của tham số `id` được truyền vào hàm.

Tóm lại, luồng hoạt động của màn hình chi tiết là một chuỗi đơn giản và rõ ràng: `UI` yêu cầu dữ liệu với một `ID` -> `ViewModel` chuyển yêu cầu -> `Repository` gọi `API` -> Dữ liệu được trả về, chuyển đổi và hiển thị lên `UI`.