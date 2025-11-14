package com.example.musicfilemanager.data

import com.example.musicfilemanager.api.ApiClient
import com.example.musicfilemanager.api.ApiResult
import com.example.musicfilemanager.api.models.MusicFileRequest
import com.example.musicfilemanager.api.models.MusicFileResponse
import com.example.musicfilemanager.api.safeApiCall
import com.example.musicfilemanager.model.Music
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Data class to hold Music File with API ID
 */
data class MusicFileWithId(
    val apiId: Int,
    val music: Music,
    val fileCode: String,
    val filePath: String? = null,
    val thumbnailPath: String? = null,
    val fileType: String? = null,
    val downloadLink: String? = null,
    val description: String? = null,
    val fileSize: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Repository for Music Files API
 * Kết hợp với cache local để tăng performance
 */
object MusicApiRepository {

    private val apiService = ApiClient.musicService

    // Cache local
    private val _musicFiles = MutableStateFlow<List<Music>>(emptyList())
    val musicFiles: StateFlow<List<Music>> = _musicFiles.asStateFlow()

    // Cache with API IDs
    private val _musicFilesWithId = MutableStateFlow<List<MusicFileWithId>>(emptyList())
    val musicFilesWithId: StateFlow<List<MusicFileWithId>> = _musicFilesWithId.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Fetch all music files from API
     */
    suspend fun fetchAllMusicFiles(): ApiResult<List<Music>> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getAllMusicFiles(page = 0, size = 100) }

        when (result) {
            is ApiResult.Success -> {
                // Convert API response to domain model with IDs
                val musicFileWithIdList = result.data.content.map { response ->
                    MusicFileWithId(
                        apiId = response.id,
                        music = response.toMusic(),
                        fileCode = response.fileCode,
                        filePath = response.filePath,
                        thumbnailPath = response.thumbnailPath,
                        fileType = response.fileType,
                        downloadLink = response.downloadLink,
                        description = response.description,
                        fileSize = response.fileSize,
                        createdAt = response.createdAt,
                        updatedAt = response.updatedAt
                    )
                }

                // Store full list with IDs
                _musicFilesWithId.value = musicFileWithIdList

                // Extract music for normal use
                _musicFiles.value = musicFileWithIdList.map { it.music }

                _isLoading.value = false
                return ApiResult.Success(_musicFiles.value)
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
     * Search music files by keyword
     */
    suspend fun searchMusicFiles(keyword: String): ApiResult<List<Music>> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.searchMusicFiles(keyword) }
        _isLoading.value = false

        return when (result) {
            is ApiResult.Success -> {
                val musicList = result.data.content.map { it.toMusic() }
                ApiResult.Success(musicList)
            }
            is ApiResult.Error -> {
                _error.value = result.message
                result
            }
            is ApiResult.Loading -> result
        }
    }

    /**
     * Filter music files by genre
     */
    suspend fun filterByGenre(genreId: Int): ApiResult<List<Music>> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.filterByGenre(genreId) }
        _isLoading.value = false

        return when (result) {
            is ApiResult.Success -> {
                val musicList = result.data.content.map { it.toMusic() }
                ApiResult.Success(musicList)
            }
            is ApiResult.Error -> {
                _error.value = result.message
                result
            }
            is ApiResult.Loading -> result
        }
    }

    /**
     * Filter music files by year
     */
    suspend fun filterByYear(year: Int): ApiResult<List<Music>> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.filterByYear(year) }
        _isLoading.value = false

        return when (result) {
            is ApiResult.Success -> {
                val musicList = result.data.content.map { it.toMusic() }
                ApiResult.Success(musicList)
            }
            is ApiResult.Error -> {
                _error.value = result.message
                result
            }
            is ApiResult.Loading -> result
        }
    }

    /**
     * Get music file by ID
     */
    suspend fun getMusicFileById(id: Int): ApiResult<Music> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getMusicFileById(id) }
        _isLoading.value = false

        return when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toMusic())
            is ApiResult.Error -> {
                _error.value = result.message
                result
            }
            is ApiResult.Loading -> result
        }
    }

    /**
     * Get music detail by ID for editing
     */
    suspend fun getMusicDetailById(id: Int): ApiResult<com.example.musicfilemanager.model.MusicDetail> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getMusicFileById(id) }
        _isLoading.value = false

        return when (result) {
            is ApiResult.Success -> {
                val data = result.data
                val detail = com.example.musicfilemanager.model.MusicDetail(
                    apiId = data.id,
                    fileCode = data.fileCode,
                    fileName = data.fileName,
                    artist = data.artist,
                    album = data.album,
                    duration = data.duration,
                    fileSize = data.fileSize,
                    genreId = data.genreId,
                    releaseYear = data.releaseYear,
                    description = data.description,
                    filePath = data.filePath,
                    fileType = data.fileType,
                    downloadLink = data.downloadLink
                )
                ApiResult.Success(detail)
            }
            is ApiResult.Error -> {
                _error.value = result.message
                result
            }
            is ApiResult.Loading -> result
        }
    }

    /**
     * Create new music file via API
     */
    suspend fun createMusicFile(
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
    ): ApiResult<Music> {
        _isLoading.value = true
        _error.value = null

        val request = MusicFileRequest(
            fileCode = fileCode,
            fileName = fileName,
            filePath = filePath,
            genreId = genreId,
            fileType = fileType,
            downloadLink = downloadLink,
            artist = artist,
            album = album,
            releaseYear = releaseYear,
            description = description,
            duration = duration,
            fileSize = fileSize
        )

        val result = safeApiCall { apiService.createMusicFile(request) }

        when (result) {
            is ApiResult.Success -> {
                // Refresh the list after creating
                fetchAllMusicFiles()
                _isLoading.value = false
                return ApiResult.Success(result.data.toMusic())
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
     * Update music file via API
     */
    suspend fun updateMusicFile(
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
    ): ApiResult<Music> {
        _isLoading.value = true
        _error.value = null

        val request = MusicFileRequest(
            fileCode = fileCode,
            fileName = fileName,
            filePath = filePath,
            genreId = genreId,
            fileType = fileType,
            downloadLink = downloadLink,
            artist = artist,
            album = album,
            releaseYear = releaseYear,
            description = description,
            duration = duration,
            fileSize = fileSize
        )

        val result = safeApiCall { apiService.updateMusicFile(id, request) }

        when (result) {
            is ApiResult.Success -> {
                // Refresh the list after updating
                fetchAllMusicFiles()
                _isLoading.value = false
                return ApiResult.Success(result.data.toMusic())
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
     * Delete music file via API
     */
    suspend fun deleteMusicFile(id: Int): ApiResult<Unit> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.deleteMusicFile(id) }

        when (result) {
            is ApiResult.Success -> {
                // Refresh the list after deleting
                fetchAllMusicFiles()
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
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Upload music file (Step 1: Upload file only)
     * Gửi fileCode tạm (server required), server sẽ trả về fileCode chính thức
     * Returns UploadResult with fileCode, downloadLink, and additional metadata
     */
    data class UploadResult(
        val fileCode: String,
        val downloadLink: String,
        val fileSize: Long? = null,
        val duration: Int? = null,
        val artist: String? = null,
        val album: String? = null
    )

    suspend fun uploadMusicFile(file: java.io.File, tempFileCode: String, fileName: String): ApiResult<UploadResult> {
        _isLoading.value = true
        _error.value = null

        try {
            // Detect MIME type từ file extension
            val mimeType = when (file.extension.lowercase()) {
                "mp3" -> "audio/mpeg"
                "wav" -> "audio/wav"
                "flac" -> "audio/flac"
                "aac" -> "audio/aac"
                "ogg" -> "audio/ogg"
                "wma" -> "audio/x-ms-wma"
                "m4a" -> "audio/mp4"
                else -> "audio/mpeg" // Default to mp3
            }

            // Create multipart file với MIME type cụ thể
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = okhttp3.MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestFile
            )

            // Create fileCode part (tạm thời, server có thể override)
            val fileCodePart = tempFileCode.toRequestBody("text/plain".toMediaTypeOrNull())

            // Create fileName part
            val fileNamePart = fileName.toRequestBody("text/plain".toMediaTypeOrNull())

            val result = safeApiCall {
                apiService.uploadMusicFile(filePart, fileCodePart, fileNamePart)
            }

            when (result) {
                is ApiResult.Success -> {
                    val fileCode = result.data.fileCode
                    val downloadLink = result.data.downloadLink
                    val fileSize = result.data.fileSize
                    val duration = result.data.duration
                    val artist = result.data.artist
                    val album = result.data.album

                    _isLoading.value = false
                    return if (fileCode != null && downloadLink != null) {
                        ApiResult.Success(
                            UploadResult(
                                fileCode = fileCode,
                                downloadLink = downloadLink,
                                fileSize = fileSize,
                                duration = duration,
                                artist = artist,
                                album = album
                            )
                        )
                    } else {
                        _error.value = "Server không trả về fileCode hoặc downloadLink"
                        ApiResult.Error(_error.value!!)
                    }
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
        } catch (e: Exception) {
            _error.value = e.message ?: "Upload failed"
            _isLoading.value = false
            return ApiResult.Error(_error.value!!)
        }
    }

    /**
     * Get MusicFileWithId by code
     */
    fun getMusicFileWithIdByCode(code: String): MusicFileWithId? {
        return _musicFilesWithId.value.find { it.fileCode == code }
    }
}

/**
 * Extension function to convert MusicFileResponse to Music domain model
 */
/**
 * Convert MusicFileResponse to Music model
 */
private fun MusicFileResponse.toMusic(): Music {
    return Music(
        id = this.fileCode,
        title = this.fileName,
        artist = this.artist ?: "Unknown Artist",
        album = this.album ?: "Unknown Album",
        duration = formatDuration(this.duration ?: 0),
        genreId = "unknown", // Sẽ được map động ở UI layer
        apiGenreId = this.genreId, // Lưu API ID để lookup sau
        apiId = this.id // Lưu database ID cho edit/delete
    )
}

/**
 * Map API genreId (Int) to UI genreCode (String)
 * NOTE: Function này không còn dùng, để UI layer tự lookup
 */
@Deprecated("Use GenreViewModel.mapGenreIdToCode() instead")
private fun mapGenreIdToCode(genreId: Int): String {
    return when (genreId) {
        1 -> "pop"
        2 -> "rock"
        3 -> "jazz"
        4 -> "hiphop"
        else -> "pop" // default fallback
    }
}

/**
 * Format duration from seconds to MM:SS
 */
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}

