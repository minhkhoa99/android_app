package com.example.musicfilemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicfilemanager.navigation.Routes
import com.example.musicfilemanager.ui.MainScreen
import com.example.musicfilemanager.ui.MusicDetailScreen
import com.example.musicfilemanager.ui.AddMusicScreen
import com.example.musicfilemanager.ui.OldMusicScreen
import com.example.musicfilemanager.ui.components.DeleteConfirmDialog
import com.example.musicfilemanager.ui.genres.AddGenreScreen
import com.example.musicfilemanager.ui.genres.GenreListScreen
import com.example.musicfilemanager.ui.genres.GenreUi
import com.example.musicfilemanager.ui.genres.GenreIcon
import com.example.musicfilemanager.ui.stats.StatsScreen
import com.example.musicfilemanager.ui.theme.AppTheme
import com.example.musicfilemanager.viewmodel.GenreViewModel
import com.example.musicfilemanager.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = Routes.Library) {
                    composable(Routes.Library) {
                        val musicViewModel: MusicViewModel = viewModel()

                        // State for delete confirmation dialog
                        var showDeleteDialog by remember { mutableStateOf(false) }
                        var musicToDelete by remember { mutableStateOf<Pair<Int, String>?>(null) } // apiId, title

                        MainScreen(
                            musicViewModel = musicViewModel,
                            onAddClick = { nav.navigate(Routes.AddMusic) },
                            onBottomItemClick = { when (it) {
                                "home" -> nav.navigate(Routes.Stats)
                                "genre" -> nav.navigate(Routes.Genres)
                                "oldmusic" -> nav.navigate(Routes.OldMusic)
                                else -> {}
                            }},
                            onItemClick = { id -> nav.navigate("detail/$id") },
                            onEditClick = { id -> 
                                // Navigate đến màn chỉnh sửa
                                nav.navigate(Routes.editMusic(id))
                            },
                            onDeleteClick = { apiId, title ->
                                // Hiển thị dialog xác nhận xóa
                                musicToDelete = Pair(apiId, title)
                                showDeleteDialog = true
                            }
                        )

                        // Delete confirmation dialog
                        if (showDeleteDialog && musicToDelete != null) {
                            DeleteConfirmDialog(
                                fileName = musicToDelete!!.second,
                                onConfirm = {
                                    // Gọi API xóa
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
                    composable(Routes.AddMusic) {
                        AddMusicScreen(
                            musicId = null, // null = chế độ thêm mới
                            onBack = { nav.popBackStack() },
                            onSaved = { nav.popBackStack() }
                        )
                    }
                    // Route cho chỉnh sửa
                    composable(
                        route = Routes.EditMusic,
                        arguments = listOf(navArgument("id"){ defaultValue = "1" })
                    ) { backStack ->
                        val id = backStack.arguments?.getString("id") ?: "1"
                        AddMusicScreen(
                            musicId = id, // truyền id để chỉnh sửa
                            onBack = { nav.popBackStack() },
                            onSaved = { nav.popBackStack() }
                        )
                    }
                    composable(Routes.Genres) {
                        GenreListScreen(
                            onAdd = { nav.navigate(Routes.AddGenre) },
                            onEdit = { id -> nav.navigate(Routes.editGenre(id)) },
                            onBottomItemClick = { when (it) {
                                "home" -> nav.navigate(Routes.Stats)
                                "library" -> nav.navigate(Routes.Library)
                                "oldmusic" -> nav.navigate(Routes.OldMusic)
                                else -> {}
                            }}
                        ) 
                    }
                    composable(Routes.AddGenre) { AddGenreScreen(onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() }) }
                    // Route cho chỉnh sửa Genre
                    composable(
                        route = Routes.EditGenre,
                        arguments = listOf(navArgument("id"){ defaultValue = "" })
                    ) { backStack ->
                        val genreViewModel: GenreViewModel = viewModel()
                        val id = backStack.arguments?.getString("id") ?: ""

                        // Lấy thông tin genre từ ViewModel với API ID
                        val genreWithId = genreViewModel.getGenreWithIdByCode(id)
                        val genreToEdit = genreWithId?.let {
                            GenreUi(
                                id = it.genre.id,
                                apiId = it.apiId,
                                name = it.genre.name,
                                description = it.description ?: "Thể loại ${it.genre.name}",
                                ageRange = it.ageRange,
                                fileCount = it.totalFiles,
                                icon = when (it.genre.id.lowercase()) {
                                    "rock" -> GenreIcon.Rock
                                    "pop" -> GenreIcon.Pop
                                    "jazz" -> GenreIcon.Jazz
                                    "hiphop", "hip hop" -> GenreIcon.HipHop
                                    else -> GenreIcon.Pop
                                }
                            )
                        }

                        AddGenreScreen(
                            genreToEdit = genreToEdit,
                            viewModel = genreViewModel,
                            onBack = { nav.popBackStack() },
                            onSaved = { nav.popBackStack() }
                        )
                    }
                    composable(Routes.Stats) {
                        StatsScreen(
                            onBack = { nav.popBackStack() },
                            onBottomItemClick = { when (it) {
                                "library" -> nav.navigate(Routes.Library)
                                "genre" -> nav.navigate(Routes.Genres)
                                "oldmusic" -> nav.navigate(Routes.OldMusic)
                                else -> {}
                            }}
                        )
                    }
                    composable(Routes.OldMusic) {
                        val musicViewModel: MusicViewModel = viewModel()

                        OldMusicScreen(
                            musicViewModel = musicViewModel,
                            onBack = { nav.popBackStack() },
                            onItemClick = { id -> nav.navigate(Routes.detail(id)) },
                            onEditClick = { id -> nav.navigate(Routes.editMusic(id)) },
                            onDeleteClick = { id ->
                                // TODO: Xóa nhạc
                            },
                            onBottomItemClick = { when (it) {
                                "home" -> nav.navigate(Routes.Stats)
                                "library" -> nav.navigate(Routes.Library)
                                "genre" -> nav.navigate(Routes.Genres)
                                else -> {}
                            }}
                        )
                    }
                    composable(
                        route = Routes.Detail,
                        arguments = listOf(navArgument("id"){ defaultValue = "1" })
                    ) { backStack ->
                        val id = backStack.arguments?.getString("id") ?: "1"
                        MusicDetailScreen(
                            musicId = id,
                            onBack = { nav.popBackStack() },
                            onEdit = { /* nav to AddMusic with id if you reuse form */ },
                            onDelete = { nav.popBackStack() }
                        )
                    }
                }
            }
        }

    }
}
