# Luồng Hoạt Động Màn Hình Quản Lý Thể Loại

Tài liệu này giải thích luồng dữ liệu, cách các thành phần tương tác, và danh sách các API được sử dụng cho các màn hình quản lý Thể loại (`GenreListScreen.kt` và `AddGenreScreen.kt`).

## Tổng Quan

Chức năng quản lý thể loại bao gồm hai màn hình chính: màn hình danh sách (`GenreListScreen`) và màn hình thêm/sửa (`AddGenreScreen`). Cả hai đều sử dụng chung `GenreViewModel` để xử lý logic và tương tác với `GenreApiRepository`. Luồng hoạt động tuân thủ kiến trúc MVVM.

**Sơ đồ luồng chạy:**

```
[UI Layer]                 [ViewModel Layer]         [Data Layer]
GenreListScreen.kt    <-->   GenreViewModel.kt  <-->  GenreApiRepository.kt  <-->  GenreApiService.kt  <-->  [Web API]
AddGenreScreen.kt     /                                |                             |
       |             /                                 |-- (Gọi API)                 |-- (Định nghĩa Endpoint)
       |-- (Gửi sự kiện:                              |                             |
       |   onSave, onUpdate)                          '---------------------------> '---------------------> (Giao tiếp HTTP)
       |
       '-- (Hiển thị form, lỗi)
                                                                   
(Hiển thị list) <----------------------------------- (Cung cấp StateFlow) <------- (Cập nhật StateFlow) <----- (Nhận dữ liệu)
```

## Danh Sách API Sử Dụng

Các API được sử dụng cho chức năng quản lý thể loại được định nghĩa trong `GenreApiService.kt`.

| Phương thức | Endpoint                  | Chức năng                               |
| :----------- | :------------------------ | :-------------------------------------- |
| `GET`        | `/api/genres`             | Lấy danh sách tất cả các thể loại.       |
| `GET`        | `/api/genres/{id}`        | Lấy thông tin một thể loại bằng ID.      |
| `POST`       | `/api/genres`             | Tạo một thể loại mới.                   |
| `PUT`        | `/api/genres/{id}`        | Cập nhật thông tin một thể loại.         |
| `DELETE`     | `/api/genres/{id}`        | Xóa một thể loại.                       |

*Lưu ý: Base URL cho các endpoint này là `http://10.0.2.2:3000` (dựa trên `AppConfig`).*

## Chi Tiết Luồng Chạy

### 1. Tầng Giao Diện (UI Layer)

#### `GenreListScreen.kt` (Màn hình danh sách)
- **Chức năng:** Hiển thị danh sách các thể loại, cho phép thực hiện các hành động như làm mới, sửa, và xóa.
- **Luồng hoạt động:**
  1.  Khi màn hình được hiển thị, `GenreViewModel` được khởi tạo và tự động gọi `loadGenres()` trong khối `init`.
  2.  UI `collectAsState()` từ các `StateFlow` trong `ViewModel` (`genresWithId`, `isLoading`, `successMessage`, `error`) để lắng nghe và cập nhật giao diện.
  3.  Nếu `isLoading` là `true` và danh sách rỗng, một `CircularProgressIndicator` sẽ được hiển thị.
  4.  Dữ liệu từ `genresWithId` được chuyển đổi thành `GenreUi` và hiển thị trong một `LazyColumn`.
  5.  Khi người dùng nhấn nút "Xóa", một `AlertDialog` sẽ hiện ra để xác nhận. Nếu xác nhận, nó sẽ gọi `viewModel.deleteGenre(id, name)`.
  6.  Khi người dùng nhấn "Sửa" hoặc "Thêm mới", nó sẽ điều hướng đến màn hình `AddGenreScreen`.
  7.  Các thông báo thành công hoặc lỗi từ `ViewModel` sẽ được hiển thị bằng `Snackbar`.

#### `AddGenreScreen.kt` (Màn hình thêm/sửa)
- **Chức năng:** Cung cấp một form để người dùng nhập thông tin và tạo mới hoặc cập nhật một thể loại.
- **Luồng hoạt động:**
  1.  Màn hình nhận một `genreToEdit` (nếu là chế độ sửa) hoặc `null` (nếu là chế độ thêm mới).
  2.  Các `TextField` được liên kết với các biến trạng thái `(code, name, desc, ageRange)`.
  3.  Khi người dùng nhấn nút "Lưu" hoặc "Cập nhật":
      - Dữ liệu đầu vào được kiểm tra (validate).
      - Nếu hợp lệ, nó sẽ gọi phương thức tương ứng trong `ViewModel`: `viewModel.createGenre(...)` hoặc `viewModel.updateGenre(...)`.
  4.  `LaunchedEffect(successMessage)` được dùng để theo dõi khi nào thao tác thành công. Khi có thông báo thành công, màn hình sẽ tự động đóng lại (`onSaved()`) sau một khoảng trễ ngắn.
  5.  Các lỗi từ API (`apiError`) hoặc lỗi validate sẽ được hiển thị cho người dùng.

### 2. Tầng ViewModel (ViewModel Layer) - `GenreViewModel.kt`

`GenreViewModel` là trung tâm xử lý logic cho cả hai màn hình.

- **Chức năng:**
  - Expose các `StateFlow` từ `Repository` cho UI.
  - Cung cấp các phương thức (`loadGenres`, `createGenre`, `updateGenre`, `deleteGenre`) để UI tương tác.
  - Quản lý các thông báo (`successMessage`) để thông báo cho người dùng về kết quả của các thao tác.

- **Luồng hoạt động (ví dụ với `createGenre`):**
  1.  Phương thức `createGenre` được gọi từ `AddGenreScreen` với các tham số người dùng đã nhập.
  2.  Nó khởi chạy một coroutine bằng `viewModelScope.launch`.
  3.  Bên trong coroutine, nó gọi `repository.createGenre(...)`.
  4.  Dựa trên kết quả (`ApiResult`) trả về từ repository:
      - Nếu là `ApiResult.Success`, nó cập nhật `_successMessage.value` với một thông báo thành công.
      - Nếu là `ApiResult.Error`, `ViewModel` không cần làm gì thêm vì `repository` đã cập nhật `error` state.
  5.  Sau khi `repository` tạo mới thành công, nó sẽ tự động gọi `fetchAllGenres()` để làm mới danh sách, và `GenreListScreen` sẽ tự động cập nhật.

**Đoạn code minh họa trong `GenreViewModel.kt`:**
```kotlin
fun createGenre(code: String, name: String, ...) {
    viewModelScope.launch {
        val result = repository.createGenre(code, name, ...)
        when (result) {
            is ApiResult.Success -> {
                _successMessage.value = "Thêm thể loại '$name' thành công!"
            }
            is ApiResult.Error -> { /* Lỗi đã được xử lý ở repository */ }
            //...
        }
    }
}
```

### 3. Tầng Dữ Liệu (Data Layer) - `GenreApiRepository.kt`

`GenreApiRepository` trừu tượng hóa việc truy cập dữ liệu thể loại.

- **Chức năng:**
  - Là nơi duy nhất trong ứng dụng tương tác trực tiếp với `GenreApiService`.
  - Quản lý cache local cho danh sách thể loại (`_genres`, `_genresWithId`).
  - Xử lý kết quả từ các cuộc gọi API và cập nhật các `StateFlow` (`_isLoading`, `_error`).
  - Tự động làm mới danh sách (`fetchAllGenres()`) sau khi thực hiện các thao tác CUD (Create, Update, Delete) để đảm bảo dữ liệu trên UI luôn đồng bộ.

- **Luồng hoạt động (ví dụ với `deleteGenre`):**
  1.  `_isLoading.value` được set thành `true`.
  2.  Gọi `apiService.deleteGenre(id)` bên trong `safeApiCall`.
  3.  Khi nhận được `ApiResult.Success`:
      a. Gọi `fetchAllGenres()` để tải lại danh sách mới nhất từ server.
      b. `fetchAllGenres()` sẽ cập nhật `_genresWithId` và `_genres`.
      c. Set `_isLoading.value` thành `false`.
  4.  Nếu nhận được `ApiResult.Error`, nó cập nhật `_error.value` và set `_isLoading.value` thành `false`.

**Đoạn code minh họa trong `GenreApiRepository.kt`:**
```kotlin
suspend fun deleteGenre(id: Int): ApiResult<Unit> {
    _isLoading.value = true
    val result = safeApiCall { apiService.deleteGenre(id) }

    when (result) {
        is ApiResult.Success -> {
            // Làm mới danh sách sau khi xóa
            fetchAllGenres() 
            _isLoading.value = false
            return ApiResult.Success(Unit)
        }
        // ... xử lý Error
    }
}
```

### 4. Tầng Dịch Vụ API (API Service Layer) - `GenreApiService.kt`

Đây là `interface` của Retrofit, định nghĩa các endpoint liên quan đến "genres".

- **Chức năng:**
  - Định nghĩa các hàm `suspend` cho mỗi hành động (GET, POST, PUT, DELETE).
  - Sử dụng các annotation của Retrofit (`@GET`, `@POST`, `@Body`, `@Path`) để cấu hình các yêu cầu HTTP.
  - `ApiClient` sẽ sử dụng interface này để tạo ra một đối tượng service có thể thực hiện các cuộc gọi mạng.

Tóm lại, luồng hoạt động cho chức năng quản lý thể loại được thiết kế theo kiến trúc một chiều, giúp mã nguồn dễ theo dõi, bảo trì và mở rộng. Dữ liệu được lấy từ API, cập nhật vào `Repository`, sau đó được `ViewModel` expose cho `UI` để hiển thị. Các hành động của người dùng trên `UI` sẽ kích hoạt các phương thức trong `ViewModel` để bắt đầu một chu trình cập nhật dữ liệu mới.