# Tích hợp API Báo cáo Thống kê

## Mô tả
Màn hình Báo cáo Thống kê (StatsScreen/Trang chủ) hiển thị các thống kê tổng quan về hệ thống quản lý file nhạc, bao gồm:
1. Dung lượng lưu trữ
2. Phân bố nhạc theo thể loại
3. Phân bố nhạc theo năm phát hành
4. Danh sách nhạc cũ (> 40 năm)

## API Endpoints

### 1. Storage Report (Dung lượng Lưu trữ)
```
GET http://localhost:3005/api/reports/storage
```

**Response:**
```json
{
  "totalFiles": 150,
  "totalStorageSize": 1610612736,
  "formattedStorageSize": "1.5 GB"
}
```

**Fields:**
- `totalFiles`: Tổng số file nhạc
- `totalStorageSize`: Tổng dung lượng (bytes)
- `formattedStorageSize`: Dung lượng đã format (human-readable)

---

### 2. Genre Report (Nhạc theo Thể loại)
```
GET http://localhost:3005/api/reports/by-genre
```

**Response:**
```json
[
  {
    "genreName": "Pop",
    "fileCount": 25,
    "totalSize": 134217728
  },
  {
    "genreName": "Rock",
    "fileCount": 15,
    "totalSize": 83886080
  }
]
```

**Fields:**
- `genreName`: Tên thể loại
- `fileCount`: Số lượng file
- `totalSize`: Tổng dung lượng thể loại (bytes)

---

### 3. Year Report (Nhạc theo Năm)
```
GET http://localhost:3005/api/reports/by-year
```

**Response:**
```json
[
  {
    "year": 2023,
    "fileCountByYear": 45
  },
  {
    "year": 2022,
    "fileCountByYear": 38
  }
]
```

**Fields:**
- `year`: Năm phát hành
- `fileCountByYear`: Số lượng file phát hành năm đó

---

### 4. Old Music Report (Nhạc Cũ)
```
GET http://localhost:3005/api/reports/old-music?minAge=40
```

**Parameters:**
- `minAge`: Số năm tối thiểu (mặc định: 40)

**Response:**
```json
[
  {
    "id": 1,
    "fileCode": "MF001",
    "fileName": "Old Song.mp3",
    "releaseYear": 1980,
    "age": 45,
    "artist": "Old Artist",
    "genreName": "Classic"
  }
]
```

**Fields:**
- `id`: ID file nhạc
- `fileCode`: Mã file
- `fileName`: Tên file
- `releaseYear`: Năm phát hành
- `age`: Tuổi của bài hát (năm)
- `artist`: Nghệ sĩ
- `genreName`: Tên thể loại

## Architecture

### 1. Models (`ReportModels.kt`)
```kotlin
data class StorageReportResponse(...)
data class GenreReportResponse(...)
data class YearReportResponse(...)
data class OldMusicReportResponse(...)
```

### 2. API Service (`ReportApiService.kt`)
```kotlin
interface ReportApiService {
    suspend fun getStorageReport(): Response<StorageReportResponse>
    suspend fun getReportByGenre(): Response<List<GenreReportResponse>>
    suspend fun getReportByYear(): Response<List<YearReportResponse>>
    suspend fun getOldMusicFiles(minAge: Int = 40): Response<List<OldMusicReportResponse>>
}
```

### 3. Repository (`ReportApiRepository.kt`)
```kotlin
object ReportApiRepository {
    // StateFlows for each report type
    val storageReport: StateFlow<StorageReportResponse?>
    val genreReports: StateFlow<List<GenreReportResponse>>
    val yearReports: StateFlow<List<YearReportResponse>>
    val oldMusicReports: StateFlow<List<OldMusicReportResponse>>
    
    // Fetch methods
    suspend fun fetchStorageReport()
    suspend fun fetchGenreReport()
    suspend fun fetchYearReport()
    suspend fun fetchOldMusicReport(minAge: Int = 40)
    suspend fun fetchAllReports(minAge: Int = 40)
}
```

### 4. ViewModel (`ReportViewModel.kt`)
```kotlin
class ReportViewModel : ViewModel() {
    // Expose repository states
    val storageReport: StateFlow<StorageReportResponse?>
    val genreReports: StateFlow<List<GenreReportResponse>>
    val yearReports: StateFlow<List<YearReportResponse>>
    val oldMusicReports: StateFlow<List<OldMusicReportResponse>>
    
    // Load methods
    fun loadAllReports(minAge: Int = 40)
}
```

### 5. UI (`StatsScreen.kt`)
```kotlin
@Composable
fun StatsScreen(
    reportViewModel: ReportViewModel = viewModel(),
    onBack: () -> Unit = {},
    onBottomItemClick: (String) -> Unit = {}
) {
    // Collect states
    val storageReport by reportViewModel.storageReport.collectAsState()
    val genreReports by reportViewModel.genreReports.collectAsState()
    val yearReports by reportViewModel.yearReports.collectAsState()
    val oldMusicReports by reportViewModel.oldMusicReports.collectAsState()
    
    // Display UI cards
    StorageCard(...)
    GenreDonutCard(...)
    YearBarChartCard(...)
    OldSongsCard(...)
}
```

## Luồng hoạt động

### 1. Initialization
```
App starts
    ↓
StatsScreen created
    ↓
ReportViewModel created
    ↓
init block calls loadAllReports()
    ↓
Repository fetches all 4 reports in parallel
    ↓
StateFlows updated
    ↓
UI auto-updates
```

### 2. Data Flow
```
API Response → Repository (StateFlow) → ViewModel (StateFlow) → UI (collectAsState)
```

### 3. UI Components

#### StorageCard
- Hiển thị NeonRing với % đã sử dụng
- Tính % = totalStorageSize / 10GB (max assumed)
- Hiển thị: Total files, Total size, Used %

#### GenreDonutCard
- DonutChart với các màu khác nhau cho mỗi thể loại
- Calculate % cho mỗi genre: fileCount / totalFiles
- Legend hiển thị genre name và %

#### YearBarChartCard
- Bar chart hiển thị số lượng file theo năm
- Lấy 6 năm gần nhất
- Gradient bars với rounded corners

#### OldSongsCard
- List các file nhạc cũ > 40 năm
- Hiển thị: fileName, releaseYear, age
- Icon ♪ cho mỗi item

## Testing

### 1. Khởi động API server
```bash
cd C:\Users\Admin\Desktop\app_music
start_music_api_server.bat
```

### 2. Test từng API endpoint
```bash
# Storage Report
curl http://localhost:3005/api/reports/storage

# Genre Report
curl http://localhost:3005/api/reports/by-genre

# Year Report
curl http://localhost:3005/api/reports/by-year

# Old Music Report
curl http://localhost:3005/api/reports/old-music?minAge=40
```

### 3. Test trên app
1. Mở ứng dụng
2. Vào màn hình "Báo cáo Thống kê" (Home)
3. Kiểm tra:
   - ✅ Loading indicator khi đang tải
   - ✅ Storage card hiển thị đúng số liệu
   - ✅ Genre donut chart với các màu sắc
   - ✅ Year bar chart với các năm
   - ✅ Old songs list hiển thị file cũ
4. Test error handling:
   - Tắt server
   - Reload screen
   - Kiểm tra hiển thị error hoặc empty state

### 4. Kiểm tra Network
- Dùng Logcat để xem API calls
- URL phải là: `http://10.0.2.2:3005/api/reports/*`
- Response status phải là 200 OK

## UI Features

### Loading State
```kotlin
if (isLoading && storageReport == null) {
    CircularProgressIndicator(color = AccentPurple)
}
```

### Empty State Handling
```kotlin
if (genreChartData.isNotEmpty()) {
    GenreDonutCard(...)
}
```

### Data Calculations
```kotlin
// Storage percentage
val storagePercent = (totalStorageSize / maxStorage).coerceIn(0f, 1f)

// Genre percentages for donut
val genreData = genreReports.map { 
    it.genreName to (it.fileCount / totalFiles)
}

// Year data (last 6 years)
val yearData = yearReports.takeLast(6)
```

## Customization

### Thay đổi max storage
```kotlin
val maxStorage = 20L * 1024 * 1024 * 1024 // 20GB
```

### Thay đổi số năm hiển thị
```kotlin
val yearChartYears = yearReports.takeLast(8) // 8 years
```

### Thay đổi minAge cho nhạc cũ
```kotlin
reportViewModel.loadAllReports(minAge = 50) // > 50 years
```

### Thêm màu cho genre chart
```kotlin
private fun neonPalette() = listOf(
    Color(0xFF5AC8FA), // Cyan
    Color(0xFFB06BF7), // Purple
    Color(0xFF60E0B8), // Green
    Color(0xFFE6A85F), // Orange
    Color(0xFFFF6B6B)  // Red (new)
)
```

## Lưu ý
- API URL: `http://10.0.2.2:3005` (cho Android Emulator)
- Tất cả API calls được cache trong Repository StateFlows
- UI tự động update khi StateFlow changes
- Loading indicator chỉ hiện khi lần đầu load (storageReport == null)
- Nếu API fail, UI sẽ hiển thị empty state (không crash)
- Charts tự động scale theo data
- Colors gradient: Cyan → Purple → Green → Orange

## Troubleshooting

### Không load được dữ liệu
1. Kiểm tra server có đang chạy không
2. Kiểm tra URL đúng không (10.0.2.2 cho emulator)
3. Xem Logcat để debug lỗi API
4. Kiểm tra database có dữ liệu không

### Charts không hiển thị
- Kiểm tra data có empty không
- Kiểm tra calculations có lỗi không (division by zero)
- Verify API response format

### Storage % luôn 0
- Kiểm tra totalStorageSize có giá trị không
- Kiểm tra maxStorage có quá lớn không
- Verify coerceIn(0f, 1f) logic

### Old music list rỗng
- Kiểm tra database có file > 40 năm không
- Thử giảm minAge xuống 20 hoặc 30
- Verify releaseYear field trong database

