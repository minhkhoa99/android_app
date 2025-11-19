# Luồng Hoạt Động Màn Hình Thống Kê (StatsScreen)

Tài liệu này giải thích luồng dữ liệu và cách các thành phần trong ứng dụng Android tương tác với nhau để hiển thị dữ liệu trên màn hình Thống kê.

## Tổng Quan

Luồng dữ liệu cho màn hình thống kê tuân theo kiến trúc MVVM (Model-View-ViewModel) phổ biến trong phát triển Android hiện đại. Dữ liệu chảy theo một chiều từ tầng dữ liệu lên tầng giao diện.

**Sơ đồ luồng chạy:**

```
[UI Layer]              [ViewModel Layer]         [Data Layer]
StatsScreen.kt  <---  ReportViewModel.kt  <---  ReportApiRepository.kt  <---  ReportApiService.kt (Retrofit)  <---  [Web API]
     |                      |                       |                                |
     |-- (Hiển thị UI)       |-- (Xử lý logic)       |-- (Gọi API)                    |-- (Định nghĩa Endpoint)
     |                      |                       |                                |
     '-- (Gửi sự kiện)      '-- (Cập nhật State)    '-- (Quản lý State dữ liệu)      '-- (Giao tiếp HTTP)
```

## Chi Tiết Luồng Chạy

### 1. Tầng Giao Diện (UI Layer) - `StatsScreen.kt`

Đây là thành phần Composable chịu trách nhiệm hiển thị giao diện người dùng.

- **Khởi tạo và Quan sát:**
  - `StatsScreen` nhận một `ReportViewModel` (thường được cung cấp bởi `viewModel()` delegate của Jetpack Compose).
  - Nó sử dụng `collectAsState()` để lắng nghe và tự động cập nhật giao diện khi có sự thay đổi từ các `StateFlow` trong `ViewModel` (ví dụ: `isLoading`, `storageReport`, `genreReports`).

- **Kích hoạt luồng lấy dữ liệu:**
  - `ReportViewModel` có một khối `init` sẽ tự động gọi phương thức `loadAllReports()` ngay khi `ViewModel` được tạo lần đầu tiên.
  - Do đó, khi `StatsScreen` được hiển thị, `ViewModel` của nó sẽ được khởi tạo và quá trình lấy dữ liệu từ API sẽ bắt đầu ngay lập tức.

- **Hiển thị dữ liệu:**
  - Dựa vào trạng thái `isLoading`, màn hình sẽ hiển thị một `CircularProgressIndicator` hoặc nội dung thống kê.
  - Dữ liệu từ các state (như `storageReport`, `genreReports`) được truyền vào các composable con (`StorageCard`, `GenreDonutCard`) để vẽ biểu đồ và hiển thị thông tin chi tiết.

**Đoạn code minh họa trong `StatsScreen.kt`:**
```kotlin
@Composable
fun StatsScreen(
    reportViewModel: ReportViewModel = viewModel(),
    // ...
) {
    // Collect states from ViewModel
    val isLoading by reportViewModel.isLoading.collectAsState()
    val storageReport by reportViewModel.storageReport.collectAsState()
    val genreReports by reportViewModel.genreReports.collectAsState()
    // ...

    // Giao diện sẽ tự cập nhật khi các state trên thay đổi
    if (isLoading && storageReport == null) {
        CircularProgressIndicator(color = AccentPurple)
    } else {
        LazyColumn(...) {
            item {
                StorageCard(
                    totalFiles = storageReport?.totalFiles ?: 0,
                    // ...
                )
            }
            // ...
        }
    }
}
```

### 2. Tầng ViewModel (ViewModel Layer) - `ReportViewModel.kt`

`ReportViewModel` đóng vai trò trung gian giữa UI và tầng dữ liệu (Repository).

- **Chức năng:**
  - Tách biệt logic nghiệp vụ khỏi UI.
  - Giữ và quản lý trạng thái của UI (loading, error, data), giúp trạng thái này sống sót qua các thay đổi cấu hình (ví dụ: xoay màn hình).
  - Cung cấp các `StateFlow` để UI có thể quan sát.

- **Luồng hoạt động:**
  1.  Khi `ReportViewModel` được khởi tạo, khối `init` của nó sẽ được thực thi.
  2.  Bên trong `init`, phương thức `loadAllReports()` được gọi.
  3.  `loadAllReports()` sử dụng `viewModelScope.launch` để thực hiện một coroutine, gọi đến `repository.fetchAllReports()`.
  4.  Nó không trực tiếp quản lý state mà chỉ ủy thác cho `ReportApiRepository` và expose các `StateFlow` từ repository ra cho UI.

**Đoạn code minh họa trong `ReportViewModel.kt`:**
```kotlin
class ReportViewModel : ViewModel() {

    private val repository = ReportApiRepository

    // Expose các StateFlow từ Repository cho UI
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val error: StateFlow<String?> = repository.error
    val storageReport: StateFlow<StorageReportResponse?> = repository.storageReport
    // ...

    init {
        // Tải tất cả báo cáo ngay khi ViewModel được tạo
        loadAllReports()
    }

    fun loadAllReports(minAge: Int = 40) {
        viewModelScope.launch {
            // Gọi xuống tầng Repository
            repository.fetchAllReports(minAge)
        }
    }
}
```

### 3. Tầng Dữ Liệu (Data Layer) - `ReportApiRepository.kt`

`ReportApiRepository` là một Singleton object, chịu trách nhiệm là nguồn dữ liệu duy nhất (Single Source of Truth) cho các báo cáo từ API.

- **Chức năng:**
  - Trừu tượng hóa việc lấy dữ liệu. `ViewModel` không cần biết dữ liệu đến từ đâu (API, database, etc.).
  - Quản lý trạng thái của việc lấy dữ liệu (loading, success, error) thông qua các `MutableStateFlow` (`_isLoading`, `_error`, `_storageReport`).
  - Gọi các phương thức từ `ReportApiService` để thực hiện các yêu cầu mạng.

- **Luồng hoạt động:**
  1.  Phương thức `fetchAllReports()` được gọi từ `ViewModel`.
  2.  Nó lần lượt gọi các hàm `fetch...Report()` khác như `fetchStorageReport()`, `fetchGenreReport()`.
  3.  Mỗi hàm `fetch...` sẽ:
      - Cập nhật `_isLoading.value = true`.
      - Gọi hàm `safeApiCall` để thực hiện yêu cầu mạng một cách an toàn, xử lý các ngoại lệ (exception).
      - `safeApiCall` sẽ gọi đến phương thức tương ứng trong `apiService` (ví dụ: `apiService.getStorageReport()`).
      - Khi nhận được kết quả, nó cập nhật các `StateFlow` tương ứng (`_storageReport.value`, `_isLoading.value = false`, `_error.value`).

**Đoạn code minh họa trong `ReportApiRepository.kt`:**
```kotlin
object ReportApiRepository {

    private val apiService = ApiClient.reportService

    // Các StateFlow để quản lý trạng thái
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    // ...

    suspend fun fetchStorageReport(): ApiResult<StorageReportResponse> {
        _isLoading.value = true
        // Gọi API thông qua service
        val result = safeApiCall { apiService.getStorageReport() }

        when (result) {
            is ApiResult.Success -> {
                _storageReport.value = result.data // Cập nhật dữ liệu
                _isLoading.value = false
            }
            is ApiResult.Error -> {
                _error.value = result.message // Cập nhật lỗi
                _isLoading.value = false
            }
            //...
        }
        return result
    }

    suspend fun fetchAllReports(minAge: Int = 40) {
        // Gọi đồng thời hoặc tuần tự các API
        fetchStorageReport()
        fetchGenreReport()
        // ...
    }
}
```

### 4. Tầng Dịch Vụ API (API Service Layer)

Lớp này bao gồm `ReportApiService.kt` và `ApiClient.kt`, sử dụng thư viện Retrofit để giao tiếp với Web API.

- **`ReportApiService.kt`:**
  - Là một `interface` của Retrofit.
  - Định nghĩa các phương thức tương ứng với các endpoint của API.
  - Sử dụng các annotation (`@GET`, `@POST`, `@Query`, etc.) để mô tả chi tiết về yêu cầu HTTP.

  **Đoạn code minh họa:**
  ```kotlin
  interface ReportApiService {
      @GET("reports/storage")
      suspend fun getStorageReport(): Response<StorageReportResponse>

      @GET("reports/by-genre")
      suspend fun getReportByGenre(): Response<List<GenreReportResponse>>
  }
  ```

- **`ApiClient.kt`:**
  - Là một Singleton object chịu trách nhiệm tạo và cấu hình đối tượng Retrofit.
  - Nó thiết lập `baseUrl` (ví dụ: `http://10.0.2.2:3005/api/`), thêm các `ConverterFactory` (như `GsonConverterFactory`), và cấu hình `OkHttpClient` (để logging, timeout).
  - Cung cấp một instance của `ReportApiService` để `Repository` có thể sử dụng.

  **Đoạn code minh họa:**
  ```kotlin
  object ApiClient {
      // ...
      val reportService: ReportApiService by lazy {
          val reportRetrofit = Retrofit.Builder()
              .baseUrl("http://10.0.2.2:3005/api/") // URL của API server
              .client(okHttpClient)
              .addConverterFactory(GsonConverterFactory.create())
              .build()

          reportRetrofit.create(ReportApiService::class.java)
      }
  }
  ```

Tóm lại, khi người dùng mở màn hình Thống kê, một chuỗi các sự kiện được kích hoạt tự động, đi từ `ViewModel` xuống `Repository`, qua `ApiService` để lấy dữ liệu từ API, sau đó cập nhật ngược lên các `StateFlow`. Cuối cùng, `StatsScreen` lắng nghe các thay đổi này và hiển thị dữ liệu mới nhất cho người dùng.