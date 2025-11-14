# ✅ CẬP NHẬT: SỬ DỤNG GENRE API THAY VÌ MOCK

## 📝 Tóm Tắt Thay Đổi

Đã cập nhật tất cả các màn hình để lấy danh sách **Thể loại** từ **Genre API** (port 3000) thông qua `GenreViewModel` thay vì dữ liệu mock từ `GenreRepository`.

---

## 🔄 Các File Đã Sửa

### 1. **MainScreen.kt** - Màn Thư Viện
**Trước:**
```kotlin
// Lấy từ mock
val availableGenres by GenreRepository.genres.collectAsState()
```

**Sau:**
```kotlin
// Lấy từ API qua GenreViewModel
fun MainScreen(
    musicViewModel: MusicViewModel = viewModel(),
    genreViewModel: GenreViewModel = viewModel(), // ← Thêm
    ...
)
val availableGenres by genreViewModel.genres.collectAsState() // ← Sửa
```

**Thay đổi:**
- ✅ Thêm parameter `genreViewModel: GenreViewModel` vào `MainScreen()`
- ✅ Sửa `GenreRepository.genres` → `genreViewModel.genres` 
- ✅ Truyền `genreViewModel` vào `MusicList()` và `MusicCard()`
- ✅ Xóa import `GenreRepository`
- ✅ Thêm import `GenreViewModel`

---

### 2. **MusicDetailScreen.kt** - Màn Chi Tiết Nhạc
**Trước:**
```kotlin
// Lấy từ mock
val genres by GenreRepository.genres.collectAsState()
```

**Sau:**
```kotlin
// Lấy từ API qua GenreViewModel
fun MusicDetailScreen(
    musicId: String = "1",
    genreViewModel: GenreViewModel = viewModel(), // ← Thêm
    ...
)
val genres by genreViewModel.genres.collectAsState() // ← Sửa
```

**Thay đổi:**
- ✅ Thêm parameter `genreViewModel: GenreViewModel`
- ✅ Sửa `GenreRepository.genres` → `genreViewModel.genres`
- ✅ Xóa import `GenreRepository`
- ✅ Thêm import `GenreViewModel`

---

### 3. **AddMusicScreen.kt** - Màn Thêm/Sửa Nhạc
**Trước:**
```kotlin
// Lấy từ mock
val availableGenres by GenreRepository.genres.collectAsState()
```

**Sau:**
```kotlin
// Lấy từ API qua GenreViewModel
val genreViewModel: GenreViewModel = viewModel()
val apiGenres by genreViewModel.genres.collectAsState()
val genresForMusic = remember(apiGenres) { apiGenres.filter { it.id != "all" } }
```

**Thay đổi:**
- ✅ Tạo instance `GenreViewModel` 
- ✅ Sửa `GenreRepository.genres` → `genreViewModel.genres`
- ✅ Thêm logic auto-select genre đầu tiên khi danh sách tải xong
- ✅ Xóa import `GenreRepository`
- ✅ Thêm import `GenreViewModel`

---

## 🎯 Lợi Ích

### Trước (Mock Data):
❌ Genres cố định trong code  
❌ Không đồng bộ với database  
❌ Không thể thêm/sửa/xóa genre động  

### Sau (API Data):
✅ Genres lấy từ server thực  
✅ Đồng bộ với database (db.json)  
✅ Tự động cập nhật khi thêm/sửa/xóa genre  
✅ Tất cả màn hình dùng chung 1 nguồn dữ liệu  

---

## 🔌 Cách Hoạt Động

```
┌─────────────────────┐
│   UI Screens        │
│  - MainScreen       │
│  - AddMusicScreen   │
│  - MusicDetailScreen│
└──────────┬──────────┘
           │ collectAsState()
┌──────────▼──────────┐
│  GenreViewModel     │
│  .genres StateFlow  │
└──────────┬──────────┘
           │ fetch
┌──────────▼──────────┐
│ GenreApiRepository  │
└──────────┬──────────┘
           │ API call
┌──────────▼──────────┐
│ json-server (3000)  │
│     db.json         │
└─────────────────────┘
```

---

## 📊 Dữ Liệu Hiển Thị

### Tabs trong Thư Viện (MainScreen):
Hiển thị các genre chips:
- **Tất cả** (All)
- **Pop Music** 
- **Rock Music**
- **Jazz**
- **Hip Hop**

*→ Lấy từ API: `GET http://localhost:3000/api/genres`*

### Dropdown trong AddMusicScreen:
Hiển thị dropdown thể loại (không có "Tất cả"):
- Pop Music
- Rock Music
- Jazz
- Hip Hop

*→ Lấy từ API: `GET http://localhost:3000/api/genres` và filter `id != "all"`*

### Tag trong MusicCard:
Hiển thị tag thể loại cho từng bài nhạc:
- "Pop", "Rock", "Jazz", "Hip Hop"

*→ Lấy từ API và match với `music.genreId`*

---

## 🧪 Cách Test

### 1. Khởi động Genre API Server
```bash
start_api_server.bat
```

Kiểm tra: http://localhost:3000/api/genres

### 2. Khởi động Music API Server
```bash
start_music_api_server.bat
```

Kiểm tra: http://localhost:3005/api/music-files

### 3. Build và Run App
```bash
.\gradlew installDebug
```

### 4. Test Cases

#### Test 1: Màn Thư Viện
- ✅ Mở tab "Thư viện"
- ✅ Kiểm tra các chip hiển thị: "Tất cả", "Pop Music", "Rock Music", "Jazz", "Hip Hop"
- ✅ Click vào từng chip để filter

#### Test 2: Màn Thêm Nhạc
- ✅ Click FAB (+) để thêm nhạc mới
- ✅ Click dropdown "Thể loại"
- ✅ Kiểm tra danh sách: "Pop Music", "Rock Music", "Jazz", "Hip Hop" (không có "Tất cả")
- ✅ Chọn 1 thể loại

#### Test 3: Màn Chi Tiết
- ✅ Click vào 1 bài nhạc
- ✅ Kiểm tra thông tin "Thể loại" hiển thị đúng (VD: "Pop Music")

#### Test 4: Tạo Genre Mới
- ✅ Vào tab "Thể loại"
- ✅ Tạo genre mới (VD: "EDM")
- ✅ Quay lại tab "Thư viện"
- ✅ Kiểm tra chip "EDM" xuất hiện
- ✅ Vào màn "Thêm Nhạc"
- ✅ Kiểm tra dropdown có "EDM"

---

## 🔧 Cấu Trúc Code

### MainScreen Component Tree:
```
MainScreen (genreViewModel)
  └─ FilterChips (availableGenres from API)
  └─ MusicList (genreViewModel)
      └─ MusicCard (genreViewModel)
          └─ Genre Tag (genres from API)
```

### AddMusicScreen Component Tree:
```
AddMusicScreen (genreViewModel local)
  └─ ExposedDropdownMenuBox
      └─ DropdownMenuItem (genresForMusic from API)
```

### MusicDetailScreen Component Tree:
```
MusicDetailScreen (genreViewModel)
  └─ Genre Info Row (genreName from API)
```

---

## 📈 So Sánh

| Aspect | Trước (Mock) | Sau (API) |
|--------|--------------|-----------|
| **Data Source** | `GenreRepository` (hard-coded) | `GenreViewModel` (API) |
| **Dynamic Update** | ❌ Không | ✅ Có |
| **Sync với DB** | ❌ Không | ✅ Có |
| **Add New Genre** | ❌ Restart app | ✅ Tự động hiển thị |
| **Single Source** | ❌ Nhiều nguồn | ✅ 1 nguồn duy nhất |
| **Real-time** | ❌ Static | ✅ Dynamic |

---

## ⚠️ Lưu Ý

### 1. **Server Phải Chạy**
Nếu server không chạy, app sẽ hiển thị danh sách rỗng hoặc dùng fallback data.

**Giải pháp:**
```bash
# Terminal 1
start_api_server.bat

# Terminal 2  
start_music_api_server.bat
```

### 2. **GenreViewModel Singleton**
Tất cả màn hình dùng chung 1 instance `GenreViewModel` nhờ Compose `viewModel()`.

→ Khi 1 màn hình fetch genres, tất cả màn hình khác đều nhận được update.

### 3. **StateFlow Auto-Update**
Khi `GenreViewModel.genres` thay đổi, tất cả UI subscribe sẽ tự động re-render.

```kotlin
val genres by genreViewModel.genres.collectAsState()
// ↑ Tự động update khi genres thay đổi
```

---

## 🎓 Học Được Gì

1. **Single Source of Truth**: Tất cả UI lấy data từ 1 nguồn duy nhất
2. **Reactive UI**: StateFlow + collectAsState → auto re-render
3. **ViewModel Sharing**: Compose `viewModel()` tự động share instance
4. **API Integration**: Kết nối UI với backend thực
5. **Separation of Concerns**: UI → ViewModel → Repository → API

---

## ✅ Kết Luận

**Hoàn thành!** Tất cả màn hình giờ đây:
- ✅ Lấy genres từ API thay vì mock
- ✅ Tự động cập nhật khi có thay đổi
- ✅ Đồng bộ với database
- ✅ Dùng chung 1 source of truth

**Build Status:** 🔄 Building...  
**Test:** ⏳ Pending server start

---

## 📞 Troubleshooting

### Lỗi: "No genres displayed"
- Kiểm tra server: http://localhost:3000/api/genres
- Xem Logcat có lỗi API không
- Restart app

### Lỗi: "Compile error"
- Clean build: `.\gradlew clean`
- Rebuild: `.\gradlew assembleDebug`

### Lỗi: "Empty dropdown"
- Đợi API load xong (có thể mất 1-2s)
- Kiểm tra network trong Logcat

**Happy Coding! 🎵**

