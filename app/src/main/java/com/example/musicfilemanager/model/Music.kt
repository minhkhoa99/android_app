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
    val apiId: Int? = null, // API ID for actions like edit, delete, detail view
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val genreId: String, // UI genre code (pop, rock, jazz, etc.)
    val apiGenreId: Int? = null, // API genre ID (1, 2, 3, 4, etc.) - để lookup động
    val coverUrl: String? = null, // dùng khi muốn load ảnh thật
    val ageRange: String? = null,
    val description: String? = null,
    val releaseYear: Int? = null,
    val fileSize: Long? = null
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
    Music(id = "1", apiId = 1, title = "Bài hát mẫu.mp3", artist = "Nghệ sĩ A", album = "Album Demo", duration = "4:00", genreId = "pop"),
    Music(id = "2", apiId = 2, title = "Rock Anthem.mp3", artist = "Ban nhạc Z", album = "The Rock Collection", duration = "3:30", genreId = "rock"),
    Music(id = "3", apiId = 3, title = "Smooth Jazz.wav", artist = "John Colthout", album = "Blue Notes", duration = "5:12", genreId = "jazz")
)
