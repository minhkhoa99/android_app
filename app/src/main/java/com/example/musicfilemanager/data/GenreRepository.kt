package com.example.musicfilemanager.data

import com.example.musicfilemanager.model.Genre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository quản lý danh sách thể loại
 * TODO: Kết nối với Room Database thực tế
 */
object GenreRepository {

    private val _genres = MutableStateFlow(Genre.getDefaultGenres())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    /**
     * Thêm thể loại mới
     */
    fun addGenre(id: String, name: String) {
        val newGenre = Genre(id, name)
        val currentList = _genres.value.toMutableList()

        // Kiểm tra trùng lặp
        if (currentList.none { it.id == id }) {
            currentList.add(newGenre)
            _genres.value = currentList
        }
    }

    /**
     * Cập nhật thể loại
     */
    fun updateGenre(id: String, newName: String) {
        val currentList = _genres.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }

        if (index != -1) {
            currentList[index] = Genre(id, newName)
            _genres.value = currentList
        }
    }

    /**
     * Xóa thể loại
     */
    fun deleteGenre(id: String) {
        // Không cho xóa "Tất cả"
        if (id == "all") return

        val currentList = _genres.value.toMutableList()
        currentList.removeIf { it.id == id }
        _genres.value = currentList
    }

    /**
     * Lấy thể loại theo ID
     */
    fun getGenreById(id: String): Genre? {
        return _genres.value.find { it.id == id }
    }

    /**
     * Lấy danh sách genres không bao gồm "Tất cả"
     */
    fun getGenresWithoutAll(): List<Genre> {
        return _genres.value.filter { it.id != "all" }
    }
}

