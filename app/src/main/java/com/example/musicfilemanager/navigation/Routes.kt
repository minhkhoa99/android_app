package com.example.musicfilemanager.navigation

object Routes {
    const val Library  = "library"
    const val Detail   = "detail/{id}"   // màn chi tiết, nhận id
    const val AddMusic = "add_music"
    const val Genres   = "genres"
    const val AddGenre = "add_genre"
    const val Stats    = "stats"

    // helper để điều hướng tới detail với id cụ thể
    fun detail(id: String) = "detail/$id"
}