package com.example.musicfilemanager.model

// Thay đổi từ enum sang data class để hỗ trợ dynamic genres
data class Genre(
    val id: String,
    val name: String
) {
    companion object {
        val All = Genre("all", "Tất cả")
        val Pop = Genre("pop", "Pop")
        val Rock = Genre("rock", "Rock")
        val Jazz = Genre("jazz", "Jazz")
        val HipHop = Genre("hiphop", "Hip Hop")

        // Danh sách mặc định
        fun getDefaultGenres() = listOf(All, Pop, Rock, Jazz, HipHop)
    }
}

data class Music(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val genreId: String, // Thay đổi từ Genre object sang genreId
    val coverUrl: String? = null // dùng khi muốn load ảnh thật
)

val sampleMusics = listOf(
    Music("1", "Bài hát mẫu.mp3", "Nghệ sĩ A", "Album Demo", "4:00", "pop"),
    Music("2", "Rock Anthem.mp3", "Ban nhạc Z", "The Rock Collection", "3:30", "rock"),
    Music("3", "Smooth Jazz.wav", "John Colthout", "Blue Notes", "5:12", "jazz")
)
