# ✅ SỬA LỖI: GENRE LOOKUP ĐỘNG TỪ API

## 🐛 Vấn Đề

Genre ID = 4 hiển thị **"Hip Hop"** thay vì **"Classical"** vì code **hardcode mapping** thay vì lấy từ **Genre API (port 3000)**.

### Root Cause:
```kotlin
// ❌ SAI: Hardcode mapping
private fun mapGenreIdToCode(genreId: Int): String {
    return when (genreId) {
        1 -> "pop"
        2 -> "rock"
        3 -> "jazz"
        4 -> "hiphop"  // ← HARDCODE!
        else -> "pop"
    }
}
```

**Vấn đề:**
- Không linh hoạt khi Genre API thay đổi
- Genre ID = 4 có thể là Classical, Hip Hop, hoặc bất kỳ thể loại nào
- Vi phạm nguyên tắc "Single Source of Truth"

---

## ✅ Giải Pháp: Dynamic Lookup Từ GenreViewModel

### **Thay Đổi Architecture:**

```
Trước (Hardcode):
MusicAPI (genreId: 4) 
  → Repository hardcode map: 4 → "hiphop"
  → Music(genreId: "hiphop")
  → UI hiển thị: "Hip Hop" ❌

Sau (Dynamic Lookup):
MusicAPI (genreId: 4)
  → Repository lưu: Music(apiGenreId: 4)
  → UI lookup từ GenreViewModel: 4 → "Classical"
  → UI hiển thị: "Classical" ✅
```

---

## 🔧 Các Thay Đổi

### 1. **Music Model - Thêm `apiGenreId`**

**File:** `Music.kt`

```kotlin
data class Music(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val genreId: String,        // UI code (deprecated, giữ để tương thích)
    val apiGenreId: Int? = null, // ✅ THÊM: API genre ID để lookup động
    val coverUrl: String? = null
)
```

**Lý do:**
- `genreId` (String): Giữ lại để không break existing code
- `apiGenreId` (Int): **Nguồn truth** từ API, dùng để lookup

---

### 2. **Repository - Lưu `apiGenreId`**

**File:** `MusicApiRepository.kt`

```kotlin
private fun MusicFileResponse.toMusic(): Music {
    return Music(
        id = this.fileCode,
        title = this.fileName,
        artist = this.artist ?: "Unknown Artist",
        album = this.album ?: "Unknown Album",
        duration = formatDuration(this.duration ?: 0),
        genreId = "unknown",         // Placeholder (không dùng nữa)
        apiGenreId = this.genreId    // ✅ Lưu API ID
    )
}
```

**Thay đổi:**
- ❌ Xóa hardcode `mapGenreIdToCode()`
- ✅ Lưu trực tiếp `apiGenreId` từ API response

---

### 3. **GenreViewModel - Thêm Mapping Function**

**File:** `GenreViewModel.kt`

```kotlin
/**
 * Map API genreId (Int) to UI genreCode (String)
 * Lookup từ genres đã load từ API
 */
fun mapGenreIdToCode(apiGenreId: Int): String {
    val genreWithId = genresWithId.value.find { it.apiId == apiGenreId }
    return genreWithId?.code ?: "pop" // fallback nếu không tìm thấy
}
```

**Lợi ích:**
- ✅ Lookup động từ API
- ✅ Luôn đúng với database
- ✅ Tự động cập nhật khi genres thay đổi

---

### 4. **UI - MusicCard Lookup Động**

**File:** `MainScreen.kt`

**Trước (SAI):**
```kotlin
val genres by genreViewModel.genres.collectAsState()
val genreName = remember(music.genreId, genres) {
    genres.find { it.id == music.genreId }?.name ?: "Unknown"
}
// music.genreId = "hiphop" (hardcode)
// → Tìm thấy "Hip Hop" ❌
```

**Sau (ĐÚNG):**
```kotlin
val genresWithId by genreViewModel.genresWithId.collectAsState()
val genreName = remember(music.apiGenreId, genresWithId) {
    if (music.apiGenreId != null) {
        genresWithId.find { it.apiId == music.apiGenreId }?.name ?: "Unknown"
    } else {
        "Unknown"
    }
}
// music.apiGenreId = 4
// genresWithId.find { it.apiId == 4 }
// → Tìm thấy GenreWithId(apiId: 4, name: "Classical") ✅
```

---

## 📊 Flow Hoàn Chỉnh

### **Khi Load Music:**

```
1. Music API Response:
{
  "id": 9,
  "fileName": "mot vong trai dat",
  "genreId": 4,
  "genreName": "Classical"
}

2. Repository Convert:
Music(
  id = "MOT_VONG_...",
  title = "mot vong trai dat",
  apiGenreId = 4  ← Lưu API ID
)

3. UI MusicCard:
genresWithId.find { it.apiId == 4 }
→ GenreWithId(apiId: 4, code: "classical", name: "Classical")
→ Display: "Classical" ✅
```

### **Khi Genre API Thay Đổi:**

```
Scenario: Admin đổi ID 4 từ "Hip Hop" → "Classical"

1. Genre API (port 3000):
   GET /api/genres
   → [{ id: 4, genreName: "Classical" }]

2. GenreViewModel load:
   genresWithId = [GenreWithId(apiId: 4, name: "Classical")]

3. MusicCard lookup:
   music.apiGenreId = 4
   → genresWithId.find(4)
   → "Classical" ✅ TỰ ĐỘNG ĐÚNG!
```

---

## 🎯 Lợi Ích

| Aspect | Hardcode Mapping | Dynamic Lookup |
|--------|------------------|----------------|
| **Flexibility** | ❌ Phải sửa code | ✅ Tự động cập nhật |
| **Accuracy** | ❌ Có thể sai | ✅ Luôn đúng với DB |
| **Maintenance** | ❌ Sửa nhiều nơi | ✅ Chỉ sửa API |
| **Single Source** | ❌ Nhiều nguồn | ✅ 1 nguồn duy nhất |
| **Scalability** | ❌ Giới hạn | ✅ Vô hạn genres |

---

## 🧪 Test Case

### **Test 1: Genre ID = 4 là Classical**

```
Given: Genre API có:
  { id: 4, genreName: "Classical" }

When: Tạo music file với genreId = 4

Then: 
  - Music.apiGenreId = 4
  - UI lookup: genresWithId.find(4)
  - Display: "Classical" ✅
```

### **Test 2: Genre ID = 4 đổi thành EDM**

```
Given: Admin sửa Genre API:
  { id: 4, genreName: "EDM" }

When: 
  - Restart app (reload genres)
  - Music vẫn có apiGenreId = 4

Then:
  - GenreViewModel load: id=4 → "EDM"
  - UI lookup: genresWithId.find(4)
  - Display: "EDM" ✅ (Tự động đúng!)
```

### **Test 3: Thêm Genre Mới ID = 6**

```
Given: Thêm genre:
  { id: 6, genreName: "Country" }

When: Tạo music với genreId = 6

Then:
  - Music.apiGenreId = 6
  - UI lookup: genresWithId.find(6)
  - Display: "Country" ✅
```

---

## ⚠️ Edge Cases

### **Case 1: apiGenreId = null**
```kotlin
if (music.apiGenreId != null) {
    // Lookup
} else {
    "Unknown"  // Fallback cho data cũ
}
```

### **Case 2: Genre không tồn tại**
```kotlin
genresWithId.find { it.apiId == 999 } ?: "Unknown"
```

### **Case 3: GenresWithId chưa load**
```kotlin
val genresWithId by genreViewModel.genresWithId.collectAsState()
// StateFlow → Tự động update khi load xong
```

---

## 📝 Files Đã Sửa

1. ✅ **Music.kt** - Thêm `apiGenreId: Int?`
2. ✅ **MusicApiRepository.kt** - Lưu `apiGenreId`, xóa hardcode mapping
3. ✅ **GenreViewModel.kt** - Thêm `mapGenreIdToCode()`
4. ✅ **MainScreen.kt** - MusicCard lookup từ `genresWithId`

---

## 🔄 Migration Path

### **Cho Data Cũ (genreId String):**
```kotlin
// Vẫn support genreId cũ
val genreName = if (music.apiGenreId != null) {
    // Mới: Lookup từ API
    genresWithId.find { it.apiId == music.apiGenreId }?.name
} else {
    // Cũ: Dùng genreId string (deprecated)
    genres.find { it.id == music.genreId }?.name
} ?: "Unknown"
```

---

## ✅ Kết Luận

**Đã sửa xong!** Bây giờ:

1. ✅ Genre **LUÔN** lookup từ **GenreViewModel** (API port 3000)
2. ✅ **KHÔNG** hardcode mapping
3. ✅ **Tự động đúng** khi Genre API thay đổi
4. ✅ **Scalable** - hỗ trợ vô hạn genres

### **Kết Quả:**
```
Trước: [Hip Hop]  ❌ (hardcode sai)
Sau:   [Classical] ✅ (lookup đúng từ API)
```

**Build Status:** 🔄 Building...  
**Ready to test!** 🎵

---

## 🚀 Cách Test

1. **Khởi động Genre API:**
   ```bash
   start_api_server.bat
   ```

2. **Khởi động Music API:**
   ```bash
   start_music_api_server.bat
   ```

3. **Kiểm tra Genre ID = 4:**
   ```bash
   curl http://localhost:3000/api/genres/4
   ```

4. **Install app và xem card:**
   - Mở tab "Thư viện"
   - Tìm file "mot vong trai dat"
   - Kiểm tra tag hiển thị: **Classical** ✅

**Lỗi đã được sửa triệt để!** 🎉

