package com.example.musicfilemanager.ui.genres

data class GenreUi(
    val id: String,
    val name: String,
    val description: String,
    val fileCount: Int = 0,
    val icon: GenreIcon = GenreIcon.Rock
)

enum class GenreIcon { Rock, Pop, Jazz, HipHop }

