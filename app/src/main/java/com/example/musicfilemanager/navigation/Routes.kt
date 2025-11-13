package com.example.musicfilemanager.navigation

object Routes {
    const val Library  = "library"
    const val Detail   = "detail/{id}"   // màn chi tiết, nhận id
    const val AddMusic = "add_music"
    const val EditMusic = "edit_music/{id}" // màn chỉnh sửa, nhận id
    const val Genres   = "genres"
    const val AddGenre = "add_genre"
    const val EditGenre = "edit_genre/{id}" // màn chỉnh sửa thể loại, nhận id
    const val Stats    = "stats"
    const val OldMusic = "old_music" // màn nhạc cũ (> 40 năm)

    // helper để điều hướng tới detail với id cụ thể
    fun detail(id: String) = "detail/$id"

    // helper để điều hướng tới edit với id cụ thể
    fun editMusic(id: String) = "edit_music/$id"

    // helper để điều hướng tới edit genre với id cụ thể
    fun editGenre(id: String) = "edit_genre/$id"
}