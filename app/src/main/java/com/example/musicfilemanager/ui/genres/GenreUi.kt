package com.example.musicfilemanager.ui.genres

data class GenreUi(
    val id: String,              // Local ID (genreCode lowercase)
    val apiId: Int? = null,      // Backend API ID for edit/delete
    val name: String,
    val description: String,
    val ageRange: String? = null,
    val fileCount: Int = 0,
    val icon: GenreIcon = GenreIcon.Rock
)

enum class GenreIcon { Rock, Pop, Jazz, HipHop }

