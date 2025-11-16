package com.example.musicfilemanager.data

import com.example.musicfilemanager.api.ApiClient
import com.example.musicfilemanager.api.ApiResult
import com.example.musicfilemanager.api.safeApiCall
import com.example.musicfilemanager.api.models.GenreReportResponse
import com.example.musicfilemanager.api.models.OldMusicReportResponse
import com.example.musicfilemanager.api.models.StorageReportResponse
import com.example.musicfilemanager.api.models.YearReportResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for Report APIs
 */
object ReportApiRepository {

    private val apiService = ApiClient.reportService

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Storage report state
    private val _storageReport = MutableStateFlow<StorageReportResponse?>(null)
    val storageReport: StateFlow<StorageReportResponse?> = _storageReport.asStateFlow()

    // Genre report state
    private val _genreReports = MutableStateFlow<List<GenreReportResponse>>(emptyList())
    val genreReports: StateFlow<List<GenreReportResponse>> = _genreReports.asStateFlow()

    // Year report state
    private val _yearReports = MutableStateFlow<List<YearReportResponse>>(emptyList())
    val yearReports: StateFlow<List<YearReportResponse>> = _yearReports.asStateFlow()

    // Old music report state
    private val _oldMusicReports = MutableStateFlow<List<OldMusicReportResponse>>(emptyList())
    val oldMusicReports: StateFlow<List<OldMusicReportResponse>> = _oldMusicReports.asStateFlow()

    /**
     * Fetch storage report from API
     */
    suspend fun fetchStorageReport(): ApiResult<StorageReportResponse> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getStorageReport() }

        when (result) {
            is ApiResult.Success -> {
                _storageReport.value = result.data
                _isLoading.value = false
                return result
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
     * Fetch genre report from API
     */
    suspend fun fetchGenreReport(): ApiResult<List<GenreReportResponse>> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getReportByGenre() }

        when (result) {
            is ApiResult.Success -> {
                _genreReports.value = result.data
                _isLoading.value = false
                return result
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
     * Fetch year report from API
     */
    suspend fun fetchYearReport(): ApiResult<List<YearReportResponse>> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getReportByYear() }

        when (result) {
            is ApiResult.Success -> {
                _yearReports.value = result.data
                _isLoading.value = false
                return result
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
     * Fetch old music report from API
     */
    suspend fun fetchOldMusicReport(minAge: Int = 40): ApiResult<List<OldMusicReportResponse>> {
        _isLoading.value = true
        _error.value = null

        val result = safeApiCall { apiService.getOldMusicFiles(minAge) }

        when (result) {
            is ApiResult.Success -> {
                _oldMusicReports.value = result.data
                _isLoading.value = false
                return result
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
     * Fetch all reports at once
     */
    suspend fun fetchAllReports(minAge: Int = 40) {
        fetchStorageReport()
        fetchGenreReport()
        fetchYearReport()
        fetchOldMusicReport(minAge)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
}

