package com.example.musicfilemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicfilemanager.model.Genre
import com.example.musicfilemanager.model.Music
import com.example.musicfilemanager.viewmodel.MusicViewModel
import com.example.musicfilemanager.viewmodel.GenreViewModel
import com.example.musicfilemanager.ui.theme.AccentPurple
import com.example.musicfilemanager.ui.theme.ChipBg
import com.example.musicfilemanager.ui.theme.ChipSelected
import com.example.musicfilemanager.ui.theme.Gray700
import com.example.musicfilemanager.ui.theme.Gray800
import com.example.musicfilemanager.ui.theme.Gray900
import com.example.musicfilemanager.ui.theme.TagBg
import com.example.musicfilemanager.ui.theme.TextPrimary
import com.example.musicfilemanager.ui.theme.TextSecondary

fun interface OnItemClick { fun click(id: String) }

@Composable
fun MainScreen(
    musicViewModel: MusicViewModel = viewModel(),
    genreViewModel: GenreViewModel = viewModel(),
    onAddClick: () -> Unit = {},
    onBottomItemClick: (String) -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleteClick: (Int, String) -> Unit = { _, _ -> } // Pass apiId and title
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(Genre.All) }

    // Lấy danh sách genres từ API qua GenreViewModel
    val availableGenres by genreViewModel.genres.collectAsState()
    val genresWithId by genreViewModel.genresWithId.collectAsState()

    // Lấy danh sách music từ ViewModel
    val allMusicFiles by musicViewModel.musicFiles.collectAsState()
    val isLoading by musicViewModel.isLoading.collectAsState()

    // Local search filter (chỉ filter theo query, không filter genre ở đây nữa)
    val data = remember(query, allMusicFiles) {
        if (query.isNotBlank()) {
            allMusicFiles.filter {
                it.title.contains(query, ignoreCase = true) || it.artist.contains(query, true)
            }
        } else {
            allMusicFiles
        }
    }

    // Load data when screen is first shown
    LaunchedEffect(Unit) {
        musicViewModel.loadMusicFiles()
    }

    // Filter by genre when selected genre changes
    LaunchedEffect(selected) {
        if (selected.id == "all") {
            // Load all music files
            musicViewModel.loadMusicFiles()
        } else {
            // Find the API genre ID and filter
            val genreWithId = genresWithId.find { it.genre.id == selected.id }
            genreWithId?.let {
                musicViewModel.filterByGenre(it.apiId)
            }
        }
    }

    Scaffold(
        containerColor = Gray900,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = AccentPurple,
                contentColor = Color.White
            ) { Icon(Icons.Outlined.Add, null) }
        },
        bottomBar = {
            BottomNavBar(
                current = "library",
                onClick = onBottomItemClick
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .background(Gray900)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            SearchBar(query) { query = it }
            Spacer(Modifier.height(16.dp))
            FilterChips(
                items = availableGenres,
                selected = selected,
                onSelected = { selected = it }
            )
            Spacer(Modifier.height(12.dp))

            // Show loading indicator
            if (isLoading && data.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            } else {
                MusicList(
                    data = data,
                    genreViewModel = genreViewModel,
                    onItemClick = onItemClick,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun SearchBar(value: String, onChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Gray800,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
            Spacer(Modifier.width(10.dp))
            TextField(
                value = value,
                onValueChange = onChange,
                placeholder = { Text("Tìm kiếm nhạc", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    cursorColor = TextPrimary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
            Icon(Icons.Outlined.Sort, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun FilterChips(
    items: List<Genre>,
    selected: Genre,
    onSelected: (Genre) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(items) { g ->
            val isSelected = g == selected
            val bg = if (isSelected) ChipSelected else ChipBg
            val txt = if (isSelected) Color.White else TextPrimary
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable { onSelected(g) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = g.name,
                    color = txt,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun MusicList(
    data: List<Music>,
    genreViewModel: GenreViewModel,
    onItemClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleteClick: (Int, String) -> Unit = { _, _ -> } // Pass apiId and title
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 84.dp) // chừa chỗ FAB + bottom bar
    ) {
        items(data, key = { it.id }) { item ->
            MusicCard(
                music = item,
                genreViewModel = genreViewModel,
                onClick = {
                    // Truyền apiId thay vì fileCode cho detail screen
                    item.apiId?.let { apiId ->
                        onItemClick(apiId.toString())
                    }
                },
                onEditClick = {
                    // Truyền apiId thay vì fileCode
                    item.apiId?.let { apiId ->
                        onEditClick(apiId.toString())
                    }
                },
                onDeleteClick = {
                    // Truyền apiId và title cho delete
                    item.apiId?.let { apiId ->
                        onDeleteClick(apiId, item.title)
                    }
                }
            )
        }
    }
}

@Composable
private fun MusicCard(
    music: Music,
    genreViewModel: GenreViewModel,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    // Lấy genre name từ API qua GenreViewModel bằng apiGenreId
    val genresWithId by genreViewModel.genresWithId.collectAsState()
    val genreName = remember(music.apiGenreId, genresWithId) {
        if (music.apiGenreId != null) {
            // Tìm genre theo API ID
            genresWithId.find { it.apiId == music.apiGenreId }?.name ?: "Unknown"
        } else {
            // Fallback nếu không có apiGenreId (data cũ)
            "Unknown"
        }
    }

    Surface(
        color = Gray800,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh bìa (placeholder màu tối + icon)
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gray700),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.MusicNote, contentDescription = null, tint = TextSecondary)
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = music.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W700),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${music.artist}  •  ${music.album}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = music.duration,
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.width(8.dp))

            // Tag thể loại (Pop/Rock/Jazz)
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TagBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(genreName, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.width(8.dp))

            // Nút chỉnh sửa
            IconButton(
                onClick = {
                    onEditClick()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = "Chỉnh sửa",
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Nút xóa
            IconButton(
                onClick = {
                    onDeleteClick()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Xóa",
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(current: String, onClick: (String) -> Unit) {
    NavigationBar(containerColor = Gray800) {
        val items = listOf(
            "home" to Icons.Outlined.Home,
            "library" to Icons.Outlined.MenuBook,
            "genre" to Icons.Outlined.Category,
            "oldmusic" to Icons.Outlined.MusicNote // Đổi từ settings → oldmusic
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
