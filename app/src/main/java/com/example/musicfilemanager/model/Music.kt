package com.example.musicfilemanager.model

enum class Genre { Pop, Rock, Jazz, All }

data class Music(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val genre: Genre,
    val coverUrl: String? = null // dùng khi muốn load ảnh thật
)

val sampleMusics = listOf(
    Music("1", "Bài hát mẫu.mp3", "Nghệ sĩ A", "Album Demo", "4:00", Genre.Pop),
    Music("2", "Rock Anthem.mp3", "Ban nhạc Z", "The Rock Collection", "3:30", Genre.Rock),
    Music("3", "Smooth Jazz.wav", "John Colthout", "Blue Notes", "5:12", Genre.Jazz)
)
