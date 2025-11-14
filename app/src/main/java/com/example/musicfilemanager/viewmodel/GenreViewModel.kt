package com.example.musicfilemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicfilemanager.api.ApiResult
import com.example.musicfilemanager.data.GenreApiRepository
import com.example.musicfilemanager.model.Genre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Genre screen
 * Manages UI state and business logic
 */
class GenreViewModel : ViewModel() {

    private val repository = GenreApiRepository

    // UI States
    val genres: StateFlow<List<Genre>> = repository.genres
    val genresWithId = repository.genresWithId
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val error: StateFlow<String?> = repository.error

    // Selected genre for editing
    private val _selectedGenre = MutableStateFlow<Genre?>(null)
    val selectedGenre: StateFlow<Genre?> = _selectedGenre.asStateFlow()

    // Success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        // Load genres when ViewModel is created
        loadGenres()
    }

    /**
     * Load all genres from API
     */
    fun loadGenres() {
        viewModelScope.launch {
            repository.fetchAllGenres()
        }
    }

    /**
     * Create new genre
     */
    fun createGenre(
        code: String,
        name: String,
        description: String? = null,
        ageRange: String? = null
    ) {
        viewModelScope.launch {
            val result = repository.createGenre(code, name, description, ageRange)
            when (result) {
                is ApiResult.Success -> {
                    _successMessage.value = "Thêm thể loại '$name' thành công!"
                }
                is ApiResult.Error -> {
                    // Error is already set in repository
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    /**
     * Update existing genre
     */
    fun updateGenre(
        id: Int,
        code: String,
        name: String,
        description: String? = null,
        ageRange: String? = null
    ) {
        viewModelScope.launch {
            val result = repository.updateGenre(id, code, name, description, ageRange)
            when (result) {
                is ApiResult.Success -> {
                    _successMessage.value = "Cập nhật thể loại '$name' thành công!"
                    _selectedGenre.value = null
                }
                is ApiResult.Error -> {
                    // Error is already set in repository
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    /**
     * Delete genre
     */
    fun deleteGenre(id: Int, name: String) {
        viewModelScope.launch {
            val result = repository.deleteGenre(id)
            when (result) {
                is ApiResult.Success -> {
                    _successMessage.value = "Xóa thể loại '$name' thành công!"
                }
                is ApiResult.Error -> {
                    // Error is already set in repository
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    /**
     * Select genre for editing
     */
    fun selectGenre(genre: Genre) {
        _selectedGenre.value = genre
    }

    /**
     * Clear selected genre
     */
    fun clearSelectedGenre() {
        _selectedGenre.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        repository.clearError()
    }

    /**
     * Get genres without "Tất cả"
     */
    fun getGenresWithoutAll(): List<Genre> {
        return repository.getGenresWithoutAll()
    }

    /**
     * Get GenreWithId by code for edit/delete
     */
    fun getGenreWithIdByCode(code: String) = repository.getGenreWithIdByCode(code)

    /**
     * Map API genreId (Int) to UI genreCode (String)
     * Lookup từ genres đã load từ API
     */
    fun mapGenreIdToCode(apiGenreId: Int): String {
        val genreWithId = genresWithId.value.find { it.apiId == apiGenreId }
        return genreWithId?.code ?: "pop" // fallback to "pop" if not found
    }
}

