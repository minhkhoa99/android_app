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
    val id: String, // fileCode for UI display
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val genreId: String, // UI genre code (pop, rock, jazz, etc.)
    val apiGenreId: Int? = null, // API genre ID (1, 2, 3, 4, etc.) - để lookup động
    val apiId: Int? = null, // API database ID for editing/deleting
    val coverUrl: String? = null // dùng khi muốn load ảnh thật
)

/**
 * MusicDetail - Full information for editing
 */
data class MusicDetail(
    val apiId: Int, // API database ID
    val fileCode: String,
    val fileName: String,
    val artist: String?,
    val album: String?,
    val duration: Int?, // in seconds
    val fileSize: Long?, // in bytes
    val genreId: Int, // API genre ID
    val releaseYear: Int?,
    val description: String?,
    val filePath: String?,
    val fileType: String?,
    val downloadLink: String?
)

val sampleMusics = listOf(
    Music("1", "Bài hát mẫu.mp3", "Nghệ sĩ A", "Album Demo", "4:00", "pop"),
    Music("2", "Rock Anthem.mp3", "Ban nhạc Z", "The Rock Collection", "3:30", "rock"),
    Music("3", "Smooth Jazz.wav", "John Colthout", "Blue Notes", "5:12", "jazz")
)
