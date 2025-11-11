package com.example.musicfilemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.musicfilemanager.navigation.Routes
import com.example.musicfilemanager.ui.MainScreen
import com.example.musicfilemanager.ui.MusicDetailScreen
import com.example.musicfilemanager.ui.AddMusicScreen
import com.example.musicfilemanager.ui.genres.AddGenreScreen
import com.example.musicfilemanager.ui.genres.GenreListScreen
import com.example.musicfilemanager.ui.stats.StatsScreen
import com.example.musicfilemanager.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = Routes.Library) {
                    composable(Routes.Library) {
                        MainScreen(
                            onAddClick = { nav.navigate(Routes.AddMusic) },
                            onBottomItemClick = { when (it) {
                                "home" -> nav.navigate(Routes.Stats)
                                "genre" -> nav.navigate(Routes.Genres)
                                "settings" -> {}
                                else -> {}
                            }},
                            onItemClick = { id -> nav.navigate("detail/$id") },
                            onEditClick = { id -> 
                                // TODO: Navigate to edit screen hoặc reuse AddMusicScreen với id
                                nav.navigate("detail/$id") 
                            },
                            onDeleteClick = { id ->
                                // TODO: Hiển thị dialog xác nhận xóa
                                // Sau khi xóa, reload danh sách
                            }
                        )
                    }
                    composable(Routes.AddMusic) { AddMusicScreen(onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() }) }
                    composable(Routes.Genres) { 
                        GenreListScreen(
                            onAdd = { nav.navigate(Routes.AddGenre) },
                            onBottomItemClick = { when (it) {
                                "home" -> nav.navigate(Routes.Stats)
                                "library" -> nav.navigate(Routes.Library)
                                "settings" -> {}
                                else -> {}
                            }}
                        ) 
                    }
                    composable(Routes.AddGenre) { AddGenreScreen(onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() }) }
                    composable(Routes.Stats) { 
                        StatsScreen(
                            onBack = { nav.popBackStack() },
                            onBottomItemClick = { when (it) {
                                "library" -> nav.navigate(Routes.Library)
                                "genre" -> nav.navigate(Routes.Genres)
                                "settings" -> {}
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
