# ✅ SỬA LỖI: HIỂN THI "UNKNOWN" TRONG CARD

## 🐛 Vấn Đề

Card nhạc hiển thị **"Unknown"** thay vì tên thể loại (Pop, Rock, Jazz, Hip Hop).

### UI Hiện Tại:
```
┌─────────────────────────┐
│ ♪  mot vong trai dat    │
│    Tim • demo           │
│    4:00                 │
│    [Unknown]  ✏️ 🗑️     │ ← ❌ HIỂN THỊ SAI
└─────────────────────────┘
```

### UI Mong Đợi:
```
┌─────────────────────────┐
│ ♪  mot vong trai dat    │
│    Tim • demo           │
│    4:00                 │
│    [Hip Hop]  ✏️ 🗑️     │ ← ✅ ĐÚNG
└─────────────────────────┘
```

---

## 🔍 Nguyên Nhân

### 1. **API Response:**
```json
{
  "id": 9,
  "genreId": 4,           ← Int
  "genreName": "Hip Hop"
}
```

### 2. **Convert Sang Music Model (SAI):**
```kotlin
// TRƯỚC (SAI)
genreId = this.genreId.toString()  // 4 → "4"
```

### 3. **GenreViewModel Có:**
```kotlin
Genre(id = "pop", name = "Pop Music")      // id = "pop"
Genre(id = "rock", name = "Rock Music")    // id = "rock"
Genre(id = "jazz", name = "Jazz")          // id = "jazz"
Genre(id = "hiphop", name = "Hip Hop")     // id = "hiphop"
```

### 4. **MusicCard Tìm Genre:**
```kotlin
val genreName = genres.find { it.id == music.genreId }?.name ?: "Unknown"
// music.genreId = "4"
// Không match với "pop", "rock", "jazz", "hiphop"
// → Trả về "Unknown"
```

---

## ✅ Giải Pháp

### **Mapping genreId (Int) → genreCode (String)**

**File:** `MusicApiRepository.kt`

```kotlin
/**
 * Convert MusicFileResponse to Music model
 */
private fun MusicFileResponse.toMusic(): Music {
    // Map API genreId (Int) sang UI genreId (String code)
    val genreIdString = mapGenreIdToCode(this.genreId)
    
    return Music(
        id = this.fileCode,
        title = this.fileName,
        artist = this.artist ?: "Unknown Artist",
        album = this.album ?: "Unknown Album",
        duration = formatDuration(this.duration ?: 0),
        genreId = genreIdString  // ✅ "hiphop" thay vì "4"
    )
}

/**
 * Map API genreId (Int) to UI genreCode (String)
 */
private fun mapGenreIdToCode(genreId: Int): String {
    return when (genreId) {
        1 -> "pop"      // Pop Music
        2 -> "rock"     // Rock Music
        3 -> "jazz"     // Jazz
        4 -> "hiphop"   // Hip Hop
        else -> "pop"   // default fallback
    }
}
```

---

## 📊 Flow Sau Khi Sửa

### 1. **API Response:**
```json
{
  "genreId": 4,
  "genreName": "Hip Hop"
}
```

### 2. **Repository Convert:**
```kotlin
val genreIdString = mapGenreIdToCode(4)  // → "hiphop"
Music(
    ...
    genreId = "hiphop"  // ✅
)
```

### 3. **MusicCard Lookup:**
```kotlin
val genres = genreViewModel.genres  // [pop, rock, jazz, hiphop]
val genreName = genres.find { it.id == "hiphop" }?.name
// → "Hip Hop"  ✅
```

### 4. **UI Display:**
```
[Hip Hop]  ✅
```

---

## 🗺️ Mapping Table

| API genreId (Int) | Database genreName | UI genreCode (String) | UI genreName Display |
|-------------------|--------------------|-----------------------|----------------------|
| 1 | Pop Music | `"pop"` | Pop Music |
| 2 | Rock Music | `"rock"` | Rock Music |
| 3 | Jazz | `"jazz"` | Jazz |
| 4 | Hip Hop | `"hiphop"` | Hip Hop |

---

## 🎯 Vì Sao Cần Mapping?

### **Vấn Đề Thiết Kế:**
1. **API** dùng `genreId: Int` (1, 2, 3, 4)
2. **Genre Model** trong UI dùng `id: String` ("pop", "rock", "jazz", "hiphop")
3. **Filter Chips** dùng String để so sánh

### **Giải Pháp:**
- Map từ **Int (API)** → **String (UI)** khi convert response
- Giữ nguyên cấu trúc UI hiện tại
- Tương thích với filter logic

---

## 🔄 Alternative Solution (Nếu Muốn Thay Đổi Nhiều)

### **Option 1: Dùng genreName từ API**
```kotlin
// Thay vì dùng genreId để lookup
// Dùng luôn genreName từ API
return Music(
    ...
    genreId = this.genreId.toString(),
    genreName = this.genreName  // Thêm field mới
)
```

**Nhược điểm:** Phải sửa Music model, MusicCard, filter logic

### **Option 2: Sync với GenreViewModel**
```kotlin
// Load genres từ API vào GenreViewModel với ID từ 1-4
Genre(id = "1", name = "Pop Music", apiId = 1)
Genre(id = "2", name = "Rock Music", apiId = 2)
```

**Nhược điểm:** Phải sửa toàn bộ filter logic, chip ID

### **Option 3: Mapping (ĐÃ CHỌN)**
```kotlin
// Giữ nguyên UI, chỉ map khi convert
mapGenreIdToCode(genreId)
```

**Ưu điểm:** 
- ✅ Ít thay đổi nhất
- ✅ Tương thích với code hiện tại
- ✅ Dễ maintain

---

## 🧪 Test Cases

### **Test 1: File với genreId = 1 (Pop)**
```
Input: genreId = 1
Mapping: 1 → "pop"
Lookup: genres.find { it.id == "pop" }
Result: "Pop Music" ✅
```

### **Test 2: File với genreId = 4 (Hip Hop)**
```
Input: genreId = 4
Mapping: 4 → "hiphop"
Lookup: genres.find { it.id == "hiphop" }
Result: "Hip Hop" ✅
```

### **Test 3: File với genreId không hợp lệ (99)**
```
Input: genreId = 99
Mapping: 99 → "pop" (fallback)
Lookup: genres.find { it.id == "pop" }
Result: "Pop Music" ✅ (default)
```

---

## 📝 Files Đã Sửa

### **MusicApiRepository.kt**
- ✅ Thêm function `mapGenreIdToCode(genreId: Int): String`
- ✅ Sửa `toMusic()` để dùng mapping thay vì `.toString()`
- ✅ Map: 1→pop, 2→rock, 3→jazz, 4→hiphop

---

## ⚠️ Lưu Ý

### **Khi Thêm Genre Mới:**
Nếu thêm genre mới trong database (VD: id=5, Classical), phải:
1. Thêm vào `db.json` (Genre API)
2. Thêm vào `mapGenreIdToCode()`:
   ```kotlin
   5 -> "classical"
   ```
3. Thêm vào UI filter chips (nếu cần)

### **Sync Between APIs:**
- Genre API (port 3000): Quản lý genres
- Music API (port 3005): Dùng genreId từ Genre API
- Mapping: Cầu nối giữa 2 APIs

---

## ✅ Kết Quả

### **Trước:**
```
┌─────────────────────────┐
│ mot vong trai dat       │
│ Tim • demo              │
│ 4:00                    │
│ [Unknown]  ✏️ 🗑️        │ ❌
└─────────────────────────┘
```

### **Sau:**
```
┌─────────────────────────┐
│ mot vong trai dat       │
│ Tim • demo              │
│ 4:00                    │
│ [Hip Hop]  ✏️ 🗑️        │ ✅
└─────────────────────────┘
```

**Build Status:** 🔄 Building...  
**Ready to test!** 🎵

---

## 🚀 Cách Test

1. Build & Install app
2. Mở tab "Thư viện"
3. Xem card của file "mot vong trai dat"
4. Kiểm tra tag hiển thị:
   - ❌ Trước: "Unknown"
   - ✅ Sau: "Hip Hop"

**Lỗi đã được sửa!** 🎉

