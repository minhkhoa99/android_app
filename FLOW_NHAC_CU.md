# Luồng Hoạt Động Màn Hình Nhạc Cũ (OldMusicScreen)

Tài liệu này giải thích luồng dữ liệu, cách các thành phần tương tác, và API được sử dụng cho màn hình "Nhạc Cũ" (`OldMusicScreen.kt`), nơi hiển thị các bài hát có độ tuổi trên 40 năm.

## Tổng Quan

Màn hình này có chức năng hiển thị một danh sách các bài hát được coi là "cũ" dựa trên một tiêu chí về độ tuổi (mặc định là trên 40 năm). Dữ liệu được lấy từ một endpoint API báo cáo chuyên dụng.

**Sơ đồ luồng chạy:**

```
[UI Layer]             [ViewModel Layer]         [Data Layer]
OldMusicScreen.kt ---> MusicViewModel.kt   ---> MusicApiRepository.kt  ---> MusicApiService.kt  ---> [Web API]
      |                      |                         |                          |
      |-- (Yêu cầu data)     |-- (Gọi getForAge40Plus)  |-- (Gọi API)               |-- (Định nghĩa Endpoint)
      |                      |                         |                          |
      '--------------------->'------------------------>'------------------------->'
                                                                             (GET /api/music-files/filter/for-age-40-plus)

(Hiển thị list) <---- (Trả về List) <---- (Trả về Result) <---- (Trả về Response) <---- (API Server)
```
**Lưu ý:** Dựa trên phân tích code, `OldMusicScreen` đang sử dụng `MusicViewModel` và một endpoint trong `MusicApiService` (`/api/music-files/filter/for-age-40-plus`) để lấy dữ liệu. Điều này hơi khác so với `StatsScreen` (màn hình thống kê) vốn sử dụng `ReportViewModel` và endpoint `/api/reports/old-music`. Cả hai API này có thể cùng trả về một loại dữ liệu. Tài liệu này sẽ mô tả luồng chạy thực tế của `OldMusicScreen`.

## Danh Sách API Sử Dụng

Màn hình này sử dụng một endpoint API chính được định nghĩa trong `MusicApiService.kt`.

| Phương thức | Endpoint                                   | Chức năng                                       |
| :----------- | :----------------------------------------- | :---------------------------------------------- |
| `GET`        | `/api/music-files/filter/for-age-40-plus`  | Lấy danh sách các file nhạc có độ tuổi trên 40. |

*Lưu ý: Base URL cho endpoint này là `http://10.0.2.2:3005`.*

## Chi Tiết Luồng Chạy

### 1. Tầng Giao Diện (UI Layer) - `OldMusicScreen.kt`

Đây là Composable chịu trách nhiệm hiển thị danh sách các bài nhạc cũ.

- **Chức năng:**
  - Hiển thị danh sách các bài hát thỏa mãn điều kiện "nhạc cũ".
  - Cho phép người dùng xem thông tin cơ bản như tên bài hát, nghệ sĩ, năm phát hành và "tuổi" của bài hát.
  - Điều hướng đến màn hình chi tiết khi người dùng nhấn vào một bài hát.

- **Luồng hoạt động:**
  1.  Khi màn hình được hiển thị, `LaunchedEffect(Unit)` được kích hoạt một lần.
  2.  Bên trong `LaunchedEffect`, nó gọi hàm `suspend` `musicViewModel.getForAge40Plus()`. Lệnh gọi này sẽ tạm dừng cho đến khi có kết quả từ API.
  3.  Kết quả trả về (một danh sách `MusicFileWithId`) được xử lý và chuyển đổi thành danh sách `OldMusicItem` để phù hợp với giao diện.
  4.  Danh sách `oldMusicList` này sau đó được hiển thị trong một `LazyColumn`.
  5.  Trạng thái `isLoading` và `error` từ `ViewModel` cũng được theo dõi để hiển thị `CircularProgressIndicator` hoặc thông báo lỗi.

**Đoạn code minh họa trong `OldMusicScreen.kt`:**
```kotlin
@Composable
fun OldMusicScreen(
    musicViewModel: MusicViewModel = viewModel(),
    // ...
) {
    var oldMusicList by remember { mutableStateOf<List<OldMusicItem>>(emptyList()) }
    val isLoading by musicViewModel.isLoading.collectAsState()

    // Tải dữ liệu từ API khi màn hình được tạo
    LaunchedEffect(Unit) {
        val musicFiles = musicViewModel.getForAge40Plus()
        oldMusicList = musicFiles.map { musicFile ->
            // ... chuyển đổi dữ liệu
        }
    }

    // ... Giao diện hiển thị danh sách oldMusicList
}
```

### 2. Tầng ViewModel (ViewModel Layer) - `MusicViewModel.kt`

`MusicViewModel` cung cấp phương thức để UI có thể lấy dữ liệu nhạc cũ.

- **Chức năng:**
  - Cung cấp hàm `suspend fun getForAge40Plus()` để giao diện gọi và lấy dữ liệu.
  - Hàm này hoạt động như một cầu nối, gọi xuống tầng Repository và trả kết quả trực tiếp về cho coroutine của UI.

- **Luồng hoạt động:**
  1.  Hàm `getForAge40Plus()` được gọi từ `OldMusicScreen`.
  2.  Nó gọi `repository.getForAge40Plus()`.
  3.  Khi `repository` trả về `ApiResult`, nó xử lý kết quả:
      - Nếu là `ApiResult.Success`, nó trả về `result.data` (danh sách các bài hát).
      - Nếu là `ApiResult.Error` hoặc `ApiResult.Loading`, nó trả về một danh sách rỗng.

**Đoạn code minh họa trong `MusicViewModel.kt`:**
```kotlin
class MusicViewModel : ViewModel() {
    // ...
    suspend fun getForAge40Plus(): List<com.example.musicfilemanager.data.MusicFileWithId> {
        val result = repository.getForAge40Plus()
        return when (result) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> emptyList()
            is ApiResult.Loading -> emptyList()
        }
    }
}
```

### 3. Tầng Dữ Liệu (Data Layer) - `MusicApiRepository.kt`

`MusicApiRepository` là nơi thực hiện cuộc gọi API.

- **Chức năng:**
  - Trừu tượng hóa việc gọi API lấy danh sách nhạc cũ.
  - Chuyển đổi `MusicFileResponse` từ API thành `MusicFileWithId` để chứa thêm thông tin.

- **Luồng hoạt động:**
  1.  Hàm `suspend fun getForAge40Plus()` được gọi từ `ViewModel`.
  2.  Nó set `_isLoading.value = true`.
  3.  Nó gọi `safeApiCall { apiService.getForAge40Plus() }`.
  4.  Khi nhận được `ApiResult.Success`:
      - Nó duyệt qua danh sách `MusicFileResponse` và chuyển đổi mỗi phần tử thành `MusicFileWithId`.
      - Trả về `ApiResult.Success` chứa danh sách `MusicFileWithId` đã được chuyển đổi.
  5.  Nếu có lỗi, nó cập nhật `_error.value` và trả về `ApiResult.Error`.
  6.  Cuối cùng, nó set `_isLoading.value = false`.

**Đoạn code minh họa trong `MusicApiRepository.kt`:**
```kotlin
object MusicApiRepository {
    // ...
    suspend fun getForAge40Plus(): ApiResult<List<MusicFileWithId>> {
        _isLoading.value = true
        val result = safeApiCall { apiService.getForAge40Plus() }
        _isLoading.value = false

        return when (result) {
            is ApiResult.Success -> {
                val musicFileWithIdList = result.data.map { response ->
                    // ... chuyển đổi response thành MusicFileWithId
                }
                ApiResult.Success(musicFileWithIdList)
            }
            // ... xử lý Error
        }
    }
}
```

### 4. Tầng Dịch Vụ API (API Service Layer) - `MusicApiService.kt`

Interface của Retrofit này định nghĩa endpoint để lấy danh sách nhạc cũ.

- **Định nghĩa Endpoint:**
  ```kotlin
  interface MusicApiService {
      @GET("music-files/filter/for-age-40-plus")
      suspend fun getForAge40Plus(): Response<List<MusicFileResponse>>
  }
  ```
- `@GET("music-files/filter/for-age-40-plus")` chỉ định đây là một yêu cầu `GET` tới đường dẫn tương ứng để lấy danh sách các bài hát thỏa mãn điều kiện.

Tóm lại, luồng hoạt động của màn hình nhạc cũ là một luồng đọc dữ liệu đơn giản: `UI` kích hoạt một yêu cầu tải dữ liệu khi khởi tạo -> `ViewModel` và `Repository` chuyển tiếp yêu cầu này đến `API Service` -> Dữ liệu được lấy về, chuyển đổi và hiển thị lên giao diện.