package com.example.musicfilemanager.ui.genres

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicfilemanager.ui.theme.*
import com.example.musicfilemanager.viewmodel.GenreViewModel


@Composable
fun GenreListScreen(
    viewModel: GenreViewModel = viewModel(),
    onBack: () -> Unit = {},
    onAdd: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onOpen: (String) -> Unit = {},
    onBottomItemClick: (String) -> Unit = {}
) {
    // Collect states from ViewModel
    val genres by viewModel.genres.collectAsState()
    val genresWithId by viewModel.genresWithId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Delete confirmation dialog state
    var genreToDelete by remember { mutableStateOf<GenreUi?>(null) }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success message
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }

    // Show error message
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long,
                actionLabel = "Đóng"
            )
        }
    }

    // Convert Genre to GenreUi for display with API IDs
    val genresUi = remember(genres, genresWithId) {
        genres.filter { it.id != "all" }.map { genre ->
            val withId = genresWithId.find { it.genre.id == genre.id }
            GenreUi(
                id = genre.id,
                apiId = withId?.apiId,
                name = genre.name,
                description = withId?.description ?: "Thể loại ${genre.name}",
                ageRange = withId?.ageRange,
                fileCount = withId?.totalFiles ?: 0,
                icon = when (genre.id.lowercase()) {
                    "rock" -> GenreIcon.Rock
                    "pop" -> GenreIcon.Pop
                    "jazz" -> GenreIcon.Jazz
                    "hiphop", "hip hop" -> GenreIcon.HipHop
                    else -> GenreIcon.Pop
                }
            )
        }
    }

    // Show success snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            // Auto clear after showing
            kotlinx.coroutines.delay(2000)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            @Composable
            fun TopBarWithStatusBarPadding() {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Surface(
                        color = Color(0xFF252C3B),
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            IconButton(onClick = onBack) { Icon(Icons.Outlined.Menu, null, tint = TextPrimary) }
                            Text(
                                "Quản Lý Thể Loại",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            // Refresh button
                            IconButton(onClick = { viewModel.loadGenres() }) {
                                Icon(Icons.Outlined.Refresh, "Làm mới", tint = TextPrimary)
                            }
                        }
                    }
                }
            }
            TopBarWithStatusBarPadding()
        },
        floatingActionButton = { NeonFab(text = "Thêm Mới", onClick = onAdd) },
        containerColor = Gray900,
        bottomBar = {
            BottomNavBar(
                current = "genre",
                onClick = onBottomItemClick
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            when {
                isLoading && genresUi.isEmpty() -> {
                    // Initial loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AccentPurple)
                            Spacer(Modifier.height(16.dp))
                            Text("Đang tải thể loại...", color = TextSecondary)
                        }
                    }
                }
                genresUi.isEmpty() -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Category,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = TextSecondary
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Chưa có thể loại nào",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Nhấn nút 'Thêm Mới' để tạo thể loại đầu tiên",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            bottom = 100.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(genresUi, key = { it.id }) { g ->
                            GenreCard(
                                g,
                                onClick = { onOpen(g.id) },
                                onEdit = { onEdit(g.id) },
                                onDelete = {
                                    // Show confirmation dialog
                                    genreToDelete = g
                                }
                            )
                        }
                    }

                    // Loading overlay when refreshing
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            LinearProgressIndicator(
                                color = AccentPurple,
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        genreToDelete?.let { genre ->
            AlertDialog(
                onDismissRequest = { genreToDelete = null },
                title = { Text("Xóa Thể Loại?") },
                text = {
                    Text("Bạn có chắc muốn xóa thể loại \"${genre.name}\"?\n\nThao tác này không thể hoàn tác.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            genre.apiId?.let { id ->
                                viewModel.deleteGenre(id, genre.name)
                            }
                            genreToDelete = null
                        }
                    ) {
                        Text("Xóa", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { genreToDelete = null }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
private fun GenreCard(
    g: GenreUi,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Gray800,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon trái
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Gray700),
                contentAlignment = Alignment.Center
            ) {
                when (g.icon) {
                    GenreIcon.Rock -> Icon(Icons.Outlined.Piano, null, tint = TextPrimary)
                    GenreIcon.Pop -> Icon(Icons.Outlined.MusicNote, null, tint = TextPrimary)
                    GenreIcon.Jazz -> Icon(Icons.Outlined.Piano, null, tint = TextPrimary)
                    GenreIcon.HipHop -> Icon(Icons.Outlined.Mic, null, tint = TextPrimary)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(g.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    g.description,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "(${g.fileCount} Files)",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.width(12.dp))

            // Nút tròn nhỏ
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallRoundButton(icon = Icons.Outlined.Edit, onClick = onEdit)
                SmallRoundButton(icon = Icons.Outlined.Delete, onClick = onDelete)
            }
        }
    }
}

@Composable
private fun SmallRoundButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Gray700,
        tonalElevation = 1.dp
    ) {
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = TextPrimary)
        }
    }
}

@Composable
private fun NeonFab(text: String, onClick: () -> Unit) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF5AC8FA), Color(0xFFB06BF7)))
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = Color.Transparent,
        contentColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .background(gradient, RoundedCornerShape(28.dp))
    ) { Text(text) }
}

/* ---------- sample data để thấy UI ---------- */
private fun sampleGenres() = listOf(
    GenreUi(id = "rock", apiId = 1, name = "ROCK", description = "Nhạc rock bùng nổ", ageRange = "13+", fileCount = 50, icon = GenreIcon.Rock),
    GenreUi(id = "pop", apiId = 2, name = "POP", description = "Nhạc pop hiện đại", ageRange = "All Ages", fileCount = 120, icon = GenreIcon.Pop),
    GenreUi(id = "jazz", apiId = 3, name = "JAZZ", description = "Nhạc Jazz ngẫu hứng", ageRange = "All Ages", fileCount = 30, icon = GenreIcon.Jazz),
    GenreUi(id = "hiphop", apiId = 4, name = "HIP HOP", description = "Văn hóa Hip Hop", ageRange = "16+", fileCount = 75, icon = GenreIcon.HipHop)
)

@Composable
private fun BottomNavBar(current: String, onClick: (String) -> Unit) {
    NavigationBar(containerColor = Gray800) {
        val items = listOf(
            "home" to Icons.Outlined.Home,
            "library" to Icons.Outlined.MenuBook,
            "genre" to Icons.Outlined.Category,
            "oldmusic" to Icons.Outlined.MusicNote
        )
        items.forEach { (id, icon) ->
            NavigationBarItem(
                selected = id == current,
                onClick = { onClick(id) },
                icon = {
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null)
                    }
                },
                label = {
                    Text(
                        when (id) {
                            "home" -> "Trang chủ"
                            "library" -> "Thư viện"
                            "genre" -> "Thể loại"
                            "oldmusic" -> "Nhạc cũ"
                            else -> "Khác"
                        }
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentPurple,
                    selectedTextColor = AccentPurple,
                    indicatorColor = Gray700,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}
