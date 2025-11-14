package com.example.musicfilemanager.data

import com.example.musicfilemanager.api.ApiClient
import com.example.musicfilemanager.api.ApiResult
import com.example.musicfilemanager.api.models.GenreRequest
import com.example.musicfilemanager.api.models.GenreResponse
import com.example.musicfilemanager.api.safeApiCall
import com.example.musicfilemanager.model.Genre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data class to hold Genre with API ID
 */
data class GenreWithId(
    val apiId: Int,
    val genre: Genre,
    val description: String? = null,
    val ageRange: String? = null,
    val totalFiles: Int = 0
) {
    // Helper properties để truy cập dễ hơn
    val code: String get() = genre.id
    val name: String get() = genre.name
}

/**
 * Repository mới sử dụng API thực
 * Kết hợp với cache local để tăng performance
 */
object GenreApiRepository {

    private val apiService = ApiClient.genreService

    // Cache local
    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    // Cache with API IDs
    private val _genresWithId = MutableStateFlow<List<GenreWithId>>(emptyList())
    val genresWithId: StateFlow<List<GenreWithId>> = _genresWithId.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Fetch all genres from API
     */
    suspend fun fetchAllGenres(): ApiResult<List<Genre>> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getAllGenres() }

        when (result) {
            is ApiResult.Success -> {
                // Convert API response to domain model with IDs
                val genreWithIdList = result.data.map { response ->
                    GenreWithId(
                        apiId = response.id,
                        genre = Genre(
                            id = response.genreCode.lowercase(),
                            name = response.genreName
                        ),
                        description = response.description,
                        ageRange = response.ageRange,
                        totalFiles = response.totalFiles
                    )
                }

                // Store full list with IDs
                _genresWithId.value = genreWithIdList

                // Extract genres for normal use
                val genreList = genreWithIdList.map { it.genre }

                // Add "Tất cả" as first item
                val allGenres = mutableListOf(Genre.All).apply {
                    addAll(genreList)
                }

                _genres.value = allGenres
                _isLoading.value = false
                return ApiResult.Success(allGenres)
            }
            is ApiResult.Error -> {
                _error.value = result.message
                _isLoading.value = false

                // Fallback to default genres if API fails
                if (_genres.value.isEmpty()) {
                    _genres.value = Genre.getDefaultGenres()
                }
                return result
            }
            is ApiResult.Loading -> {
                return result
            }
        }
    }

    /**
     * Get genre by ID from API
     */
    suspend fun getGenreById(id: Int): ApiResult<Genre> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getGenreById(id) }
        _isLoading.value = false

        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toGenre())
            is ApiResult.Error -> {
                _error.value = result.message
                result
            }
            is ApiResult.Loading -> result
        }
    }

    /**
     * Get genre by code from API
     */
    suspend fun getGenreByCode(code: String): ApiResult<Genre> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getGenreByCode(code) }
        _isLoading.value = false

        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toGenre())
            is ApiResult.Error -> {
                _error.value = result.message
                result
            }
            is ApiResult.Loading -> result
        }
    }

    /**
     * Create new genre via API
     */
    suspend fun createGenre(
        code: String,
        name: String,
        description: String? = null,
        ageRange: String? = null
    ): ApiResult<Genre> {
        _isLoading.value = true
        _error.value = null

        val request = GenreRequest(
            genreCode = code,
            genreName = name,
            description = description,
            ageRange = ageRange
        )

        val result = safeApiCall { apiService.createGenre(request) }

        when (result) {
            is ApiResult.Success -> {
                // Refresh the list after creating
                fetchAllGenres()
                _isLoading.value = false
                return ApiResult.Success(result.data.toGenre())
            }
            is ApiResult.Error -> {
                _error.value = result.message
                _isLoading.value = false
                return result
            }
            is ApiResult.Loading -> {
                return result
            }
        }
    }

    /**
     * Update genre via API
     */
    suspend fun updateGenre(
        id: Int,
        code: String,
        name: String,
        description: String? = null,
        ageRange: String? = null
    ): ApiResult<Genre> {
        _isLoading.value = true
        _error.value = null

        val request = GenreRequest(
            genreCode = code,
            genreName = name,
            description = description,
            ageRange = ageRange
        )

        val result = safeApiCall { apiService.updateGenre(id, request) }

        when (result) {
            is ApiResult.Success -> {
                // Refresh the list after updating
                fetchAllGenres()
                _isLoading.value = false
                return ApiResult.Success(result.data.toGenre())
            }
            is ApiResult.Error -> {
                _error.value = result.message
                _isLoading.value = false
                return result
            }
            is ApiResult.Loading -> {
                return result
            }
        }
    }

    /**
     * Delete genre via API
     */
    suspend fun deleteGenre(id: Int): ApiResult<Unit> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.deleteGenre(id) }

        when (result) {
            is ApiResult.Success -> {
                // Refresh the list after deleting
                fetchAllGenres()
                _isLoading.value = false
                return ApiResult.Success(Unit)
            }
            is ApiResult.Error -> {
                _error.value = result.message
                _isLoading.value = false
                return result
            }
            is ApiResult.Loading -> {
                return result
            }
        }
    }

    /**
     * Get genres without "Tất cả"
     */
    fun getGenresWithoutAll(): List<Genre> {
        return _genres.value.filter { it.id != "all" }
    }

    /**
     * Get GenreWithId by genre code (for edit/delete)
     */
    fun getGenreWithIdByCode(code: String): GenreWithId? {
        return _genresWithId.value.find { it.genre.id == code.lowercase() }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}

/**
 * Extension function to convert API response to domain model
 */
private fun GenreResponse.toGenre(): Genre {
    return Genre(
        id = this.genreCode.lowercase(), // Use genreCode as ID
        name = this.genreName
    )
}

