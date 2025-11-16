package com.example.musicfilemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicfilemanager.data.ReportApiRepository
import com.example.musicfilemanager.api.models.GenreReportResponse
import com.example.musicfilemanager.api.models.OldMusicReportResponse
import com.example.musicfilemanager.api.models.StorageReportResponse
import com.example.musicfilemanager.api.models.YearReportResponse
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Stats/Report screen
 * Manages UI state and business logic for reports
 */
class ReportViewModel : ViewModel() {

    private val repository = ReportApiRepository

    // UI States
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val error: StateFlow<String?> = repository.error

    // Report data states
    val storageReport: StateFlow<StorageReportResponse?> = repository.storageReport
    val genreReports: StateFlow<List<GenreReportResponse>> = repository.genreReports
    val yearReports: StateFlow<List<YearReportResponse>> = repository.yearReports
    val oldMusicReports: StateFlow<List<OldMusicReportResponse>> = repository.oldMusicReports

    init {
        // Load all reports when ViewModel is created
        loadAllReports()
    }

    /**
     * Load all reports
     */
    fun loadAllReports(minAge: Int = 40) {
        viewModelScope.launch {
            repository.fetchAllReports(minAge)
        }
    }

    /**
     * Load storage report only
     */
    fun loadStorageReport() {
        viewModelScope.launch {
            repository.fetchStorageReport()
        }
    }

    /**
     * Load genre report only
     */
    fun loadGenreReport() {
        viewModelScope.launch {
            repository.fetchGenreReport()
        }
    }

    /**
     * Load year report only
     */
    fun loadYearReport() {
        viewModelScope.launch {
            repository.fetchYearReport()
        }
    }

    /**
     * Load old music report only
     */
    fun loadOldMusicReport(minAge: Int = 40) {
        viewModelScope.launch {
            repository.fetchOldMusicReport(minAge)
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        repository.clearError()
    }
}

