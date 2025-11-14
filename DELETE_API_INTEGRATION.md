# Tích Hợp API DELETE Music File

## Tóm tắt
Đã tích hợp API DELETE để xóa file nhạc với dialog xác nhận và tự động reload danh sách.

## API Endpoint
```
DELETE /api/music-files/{id}
```
- **Method**: DELETE
- **Path Parameter**: `id` (Integer) - API database ID
- **Response**: 204 No Content / 200 OK

## Thay đổi đã thực hiện

### 1. DeleteConfirmDialog.kt (MỚI)
Tạo component dialog xác nhận xóa với:
- ⚠️ Warning icon màu đỏ
- Hiển thị tên file nhạc
- 2 buttons: "Hủy" và "Xóa"
- Material Design 3 styling

```kotlin
@Composable
fun DeleteConfirmDialog(
    fileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
)
```

### 2. MainScreen.kt
#### a. Cập nhật signature của onDeleteClick
```kotlin
// ❌ Trước
onDeleteClick: (String) -> Unit = {}  // Nhận fileCode

// ✅ Sau  
onDeleteClick: (Int, String) -> Unit = { _, _ -> }  // Nhận apiId và title
```

#### b. Cập nhật MusicList
```kotlin
onDeleteClick = { 
    item.apiId?.let { apiId ->
        onDeleteClick(apiId, item.title)  // Truyền apiId và title
    }
}
```

### 3. MainActivity.kt
#### a. Thêm imports
```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.musicfilemanager.ui.components.DeleteConfirmDialog
import com.example.musicfilemanager.viewmodel.MusicViewModel
```

#### b. Implement delete logic
```kotlin
composable(Routes.Library) {
    val musicViewModel: MusicViewModel = viewModel()
    
    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var musicToDelete by remember { mutableStateOf<Pair<Int, String>?>(null) }
    
    MainScreen(
        musicViewModel = musicViewModel,
        // ...
        onDeleteClick = { apiId, title ->
            musicToDelete = Pair(apiId, title)
            showDeleteDialog = true
        }
    )
    
    // Delete confirmation dialog
    if (showDeleteDialog && musicToDelete != null) {
        DeleteConfirmDialog(
            fileName = musicToDelete!!.second,
            onConfirm = {
                musicViewModel.deleteMusicFile(
                    id = musicToDelete!!.first,
                    fileName = musicToDelete!!.second
                )
                showDeleteDialog = false
                musicToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                musicToDelete = null
            }
        )
    }
}
```

### 4. MusicViewModel.kt (Đã có sẵn)
```kotlin
fun deleteMusicFile(id: Int, fileName: String) {
    viewModelScope.launch {
        val result = repository.deleteMusicFile(id)
        when (result) {
            is ApiResult.Success -> {
                _successMessage.value = "Xóa file nhạc '$fileName' thành công!"
            }
            is ApiResult.Error -> {
                // Error is already set in repository
            }
            is ApiResult.Loading -> {}
        }
    }
}
```

### 5. MusicApiRepository.kt (Đã có sẵn)
```kotlin
suspend fun deleteMusicFile(id: Int): ApiResult<Unit> {
    _isLoading.value = true
    _error.value = null

    val result = safeApiCall { apiService.deleteMusicFile(id) }

    when (result) {
        is ApiResult.Success -> {
            // Refresh the list after deleting
            fetchAllMusicFiles()  // ✅ Auto reload
            _isLoading.value = false
            return ApiResult.Success(Unit)
        }
        // ...
    }
}
```

## Luồng hoạt động

### User Flow
```
1. User click nút Delete (icon 🗑️) ở MusicCard
   ↓
2. MainScreen.onDeleteClick được gọi với (apiId, title)
   ↓
3. State được cập nhật → Dialog hiển thị
   ↓
4. Dialog hiển thị: "Xác nhận xóa 'Tên bài hát'"
   ↓
5a. User click "Hủy" → Dialog đóng
5b. User click "Xóa" → Tiếp tục
   ↓
6. MusicViewModel.deleteMusicFile(apiId, title)
   ↓
7. Repository.deleteMusicFile(apiId)
   ↓
8. Call API: DELETE /api/music-files/{apiId}
   ↓
9. Success → fetchAllMusicFiles() (auto reload)
   ↓
10. Success message: "Xóa file nhạc 'xxx' thành công!"
   ↓
11. Dialog đóng, danh sách tự động cập nhật
```

### Data Flow
```
UI Layer (MainScreen)
  ↓ onDeleteClick(apiId, title)
  ↓
Presentation Layer (MainActivity)
  ↓ Show DeleteConfirmDialog
  ↓ User confirms
  ↓
ViewModel (MusicViewModel)
  ↓ deleteMusicFile(id, fileName)
  ↓
Data Layer (MusicApiRepository)
  ↓ safeApiCall { apiService.deleteMusicFile(id) }
  ↓
Network Layer (MusicApiService)
  ↓ DELETE /api/music-files/{id}
  ↓
  ← Response
  ↓
Data Layer (Auto reload)
  ↓ fetchAllMusicFiles()
  ↓
StateFlow updates
  ↓
UI auto updates (Compose recomposition)
```

## UI Components

### Delete Button trong MusicCard
```kotlin
IconButton(
    onClick = { onDeleteClick() },
    modifier = Modifier.size(36.dp)
) {
    Icon(
        Icons.Outlined.Delete,
        contentDescription = "Xóa",
        tint = Color(0xFFEF5350),  // Red color
        modifier = Modifier.size(20.dp)
    )
}
```

### DeleteConfirmDialog
- **Warning Icon**: 64dp, màu đỏ trong background tròn
- **Title**: "Xác nhận xóa" (Bold, Large)
- **Message**: "Bạn có chắc chắn muốn xóa file nhạc này?"
- **File Name**: Tên file được highlight (SemiBold)
- **Buttons**: 
  - Hủy (Outlined, gray)
  - Xóa (Filled, red #EF5350)

## Error Handling

### Network Error
- Repository catch error và set `_error.value`
- ViewModel nhận ApiResult.Error
- User thấy error message (nếu có UI hiển thị)

### No apiId
- Check `item.apiId?.let { }` trước khi gọi onDeleteClick
- Nếu null → không làm gì (silent fail)

### API Error Response
- 404: File không tồn tại
- 500: Server error
- Network timeout
→ Tất cả được handle bởi `safeApiCall`

## Success Message
Sau khi xóa thành công:
```kotlin
_successMessage.value = "Xóa file nhạc '$fileName' thành công!"
```

Success message có thể được hiển thị bằng:
- Snackbar (recommended)
- Toast
- Dialog
- Bottom sheet

## Auto Reload
Sau khi DELETE success:
```kotlin
is ApiResult.Success -> {
    fetchAllMusicFiles()  // ← Tự động load lại danh sách
    _isLoading.value = false
    return ApiResult.Success(Unit)
}
```

→ UI tự động cập nhật vì `musicFiles` StateFlow thay đổi

## Testing Checklist

### Functional Tests
- [ ] Click nút Delete → Dialog hiển thị
- [ ] Dialog hiển thị đúng tên file
- [ ] Click "Hủy" → Dialog đóng, không xóa
- [ ] Click "Xóa" → Call API DELETE
- [ ] Delete success → Danh sách tự động reload
- [ ] Delete success → Success message hiển thị
- [ ] File biến mất khỏi danh sách

### Edge Cases
- [ ] Delete file không tồn tại (404)
- [ ] Network error khi delete
- [ ] Delete nhiều file liên tiếp
- [ ] Click Delete khi đang loading
- [ ] apiId = null (không có nút delete hoặc disabled)

### UI/UX Tests
- [ ] Dialog animation mượt
- [ ] Dialog đóng sau khi xóa
- [ ] Loading indicator khi đang xóa
- [ ] Error message nếu xóa thất bại
- [ ] Success message sau khi xóa thành công
- [ ] Danh sách không bị scroll reset sau khi xóa

## Improvements (Optional)

### 1. Snackbar cho Success Message
```kotlin
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(successMessage) {
    successMessage?.let {
        snackbarHostState.showSnackbar(it)
        musicViewModel.clearSuccessMessage()
    }
}
```

### 2. Undo Delete
```kotlin
snackbarHostState.showSnackbar(
    message = "Đã xóa $fileName",
    actionLabel = "Hoàn tác",
    duration = SnackbarDuration.Short
)
```

### 3. Swipe to Delete
```kotlin
SwipeToDismiss(
    state = dismissState,
    directions = setOf(DismissDirection.EndToStart),
    onDismissed = { onDeleteClick(item.apiId, item.title) }
)
```

### 4. Batch Delete
```kotlin
var selectedItems by remember { mutableStateOf<Set<Int>>(emptySet()) }

fun deleteSelected() {
    selectedItems.forEach { apiId ->
        musicViewModel.deleteMusicFile(apiId, "")
    }
}
```

---
**Ngày hoàn thành**: 2025-11-14  
**Status**: ✅ Hoàn tất

