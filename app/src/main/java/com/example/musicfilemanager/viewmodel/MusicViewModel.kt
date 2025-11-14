package com.example.musicfilemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicfilemanager.api.ApiResult
import com.example.musicfilemanager.data.MusicApiRepository
import com.example.musicfilemanager.model.Music
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Music Library screen
 * Manages UI state and business logic
 */
class MusicViewModel : ViewModel() {

    private val repository = MusicApiRepository

    // UI States
    val musicFiles: StateFlow<List<Music>> = repository.musicFiles
    val musicFilesWithId = repository.musicFilesWithId
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val error: StateFlow<String?> = repository.error

    // Selected music for editing
    private val _selectedMusic = MutableStateFlow<Music?>(null)
    val selectedMusic: StateFlow<Music?> = _selectedMusic.asStateFlow()

    // Selected music detail for editing (full information)
    private val _selectedMusicDetail = MutableStateFlow<com.example.musicfilemanager.model.MusicDetail?>(null)
    val selectedMusicDetail: StateFlow<com.example.musicfilemanager.model.MusicDetail?> = _selectedMusicDetail.asStateFlow()

    // Success message
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter state
    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    init {
        // Load music files when ViewModel is created
        loadMusicFiles()
    }

    /**
     * Load all music files from API
     */
    fun loadMusicFiles() {
        viewModelScope.launch {
            repository.fetchAllMusicFiles()
        }
    }

    /**
     * Search music files by keyword
     */
    fun searchMusicFiles(keyword: String) {
        _searchQuery.value = keyword
        if (keyword.isBlank()) {
            loadMusicFiles()
            return
        }

        viewModelScope.launch {
            repository.searchMusicFiles(keyword)
        }
    }

    /**
     * Filter music files by genre
     */
    fun filterByGenre(genreId: Int?) {
        _selectedGenreId.value = genreId

        if (genreId == null) {
            loadMusicFiles()
            return
        }

        viewModelScope.launch {
            repository.filterByGenre(genreId)
        }
    }

    /**
     * Filter music files by year
     */
    fun filterByYear(year: Int) {
        viewModelScope.launch {
            repository.filterByYear(year)
        }
    }

    /**
     * Create new music file (suspend version - await result)
     */
    suspend fun createMusicFileAndWait(
        fileCode: String,
        fileName: String,
        genreId: Int,
        filePath: String? = null,
        fileType: String? = null,
        downloadLink: String? = null,
        artist: String? = null,
        album: String? = null,
        releaseYear: Int? = null,
        description: String? = null,
        duration: Int? = null,
        fileSize: Long? = null
    ): Boolean {
        val result = repository.createMusicFile(
            fileCode, fileName, genreId, filePath, fileType, downloadLink,
            artist, album, releaseYear, description, duration, fileSize
        )
        return when (result) {
            is ApiResult.Success -> {
                _successMessage.value = "Thêm file nhạc '$fileName' thành công!"
                true
            }
            is ApiResult.Error -> {
                // Error is already set in repository
                false
            }
            is ApiResult.Loading -> false
        }
    }

    /**
     * Create new music file
     */
    fun createMusicFile(
        fileCode: String,
        fileName: String,
        genreId: Int,
        filePath: String? = null,
        fileType: String? = null,
        downloadLink: String? = null,
        artist: String? = null,
        album: String? = null,
        releaseYear: Int? = null,
        description: String? = null,
        duration: Int? = null,
        fileSize: Long? = null
    ) {
        viewModelScope.launch {
            createMusicFileAndWait(
                fileCode, fileName, genreId, filePath, fileType, downloadLink,
                artist, album, releaseYear, description, duration, fileSize
            )
        }
    }

    /**
     * Update existing music file
     */
    fun updateMusicFile(
        id: Int,
        fileCode: String,
        fileName: String,
        genreId: Int,
        filePath: String? = null,
        fileType: String? = null,
        downloadLink: String? = null,
        artist: String? = null,
        album: String? = null,
        releaseYear: Int? = null,
        description: String? = null,
        duration: Int? = null,
        fileSize: Long? = null
    ) {
        viewModelScope.launch {
            val result = repository.updateMusicFile(
                id, fileCode, fileName, genreId, filePath, fileType, downloadLink,
                artist, album, releaseYear, description, duration, fileSize
            )
            when (result) {
                is ApiResult.Success -> {
                    _successMessage.value = "Cập nhật file nhạc '$fileName' thành công!"
                    _selectedMusic.value = null
                }
                is ApiResult.Error -> {
                    // Error is already set in repository
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    /**
     * Delete music file
     */
    fun deleteMusicFile(id: Int, fileName: String) {
        viewModelScope.launch {
            val result = repository.deleteMusicFile(id)
            when (result) {
                is ApiResult.Success -> {
                    _successMessage.value = "Xóa file nhạc '$fileName' thành công!"
                }
                is ApiResult.Error -> {
                    // Error is already set in repository
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    /**
     * Select music for editing
     */
    fun selectMusic(music: Music) {
        _selectedMusic.value = music
    }

    /**
     * Clear selected music
     */
    fun clearSelectedMusic() {
        _selectedMusic.value = null
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
     * Upload music file to server (Step 1)
     * Gửi tempFileCode (server required), server sẽ trả về fileCode chính thức
     * Returns UploadResult(fileCode, downloadLink) if success, null if failed
     */
    suspend fun uploadMusicFile(file: java.io.File, tempFileCode: String, fileName: String): MusicApiRepository.UploadResult? {
        val result = repository.uploadMusicFile(file, tempFileCode, fileName)
        return when (result) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> null
            is ApiResult.Loading -> null
        }
    }

    /**
     * Get music file with ID by code
     */
    fun getMusicFileWithIdByCode(code: String) = repository.getMusicFileWithIdByCode(code)

    /**
     * Load music file by ID for editing
     */
    suspend fun loadMusicFileById(id: Int): com.example.musicfilemanager.model.MusicDetail? {
        val result = repository.getMusicDetailById(id)
        return when (result) {
            is ApiResult.Success -> {
                _selectedMusicDetail.value = result.data
                result.data
            }
            is ApiResult.Error -> {
                _selectedMusicDetail.value = null
                null
            }
            is ApiResult.Loading -> null
        }
    }

    /**
     * Clear selected music detail
     */
    fun clearSelectedMusicDetail() {
        _selectedMusicDetail.value = null
    }
}

