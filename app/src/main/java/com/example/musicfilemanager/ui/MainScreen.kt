package com.example.musicfilemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicfilemanager.model.Genre
import com.example.musicfilemanager.model.Music
import com.example.musicfilemanager.model.sampleMusics
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

@Preview
@Composable
fun MainScreen(
    onAddClick: () -> Unit = {},
    onBottomItemClick: (String) -> Unit = {},
    onItemClick: (String) -> Unit = {},

) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(Genre.All) }

    val chips = listOf(Genre.All, Genre.Pop, Genre.Rock, Genre.Jazz)

    val data = remember(query, selected) {
        sampleMusics
            .filter { selected == Genre.All || it.genre == selected }
            .filter { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, true) }
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            SearchBar(query) { query = it }
            Spacer(Modifier.height(16.dp))
            FilterChips(
                items = chips,
                selected = selected,
                onSelected = { selected = it }
            )
            Spacer(Modifier.height(12.dp))
            MusicList(data)
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
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { g ->
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
                    text = when (g) {
                        Genre.All -> "Tất cả"
                        Genre.Pop -> "Pop"
                        Genre.Rock -> "Rock"
                        Genre.Jazz -> "Jazz"
                    },
                    color = txt,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun MusicList(data: List<Music>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 84.dp) // chừa chỗ FAB + bottom bar
    ) {
        items(data, key = { it.id }) { item ->
            MusicCard(item)
        }
    }
}

@Composable
private fun MusicCard(m: Music) {
    Surface(
        color = Gray800,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
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
                    text = m.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W700),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${m.artist}  •  ${m.album}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = m.duration,
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
                Text(m.genre.name, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
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
            "search" to Icons.Outlined.Search,
            "settings" to Icons.Outlined.Settings
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
                            "search" -> "Tìm kiếm"
                            else -> "Cài đặt"
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
