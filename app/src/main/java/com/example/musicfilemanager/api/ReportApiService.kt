package com.example.musicfilemanager.api

import com.example.musicfilemanager.api.models.GenreReportResponse
import com.example.musicfilemanager.api.models.OldMusicReportResponse
import com.example.musicfilemanager.api.models.StorageReportResponse
import com.example.musicfilemanager.api.models.YearReportResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service for Reports endpoints
 * Base URL: http://10.0.2.2:3005/api
 */
interface ReportApiService {

    /**
     * Get storage report
     * GET /api/reports/storage
     */
    @GET("reports/storage")
    suspend fun getStorageReport(): Response<StorageReportResponse>

    /**
     * Get report by genre
     * GET /api/reports/by-genre
     */
    @GET("reports/by-genre")
    suspend fun getReportByGenre(): Response<List<GenreReportResponse>>

    /**
     * Get report by year
     * GET /api/reports/by-year
     */
    @GET("reports/by-year")
    suspend fun getReportByYear(): Response<List<YearReportResponse>>

    /**
     * Get old music files
     * GET /api/reports/old-music?minAge=40
     */
    @GET("reports/old-music")
    suspend fun getOldMusicFiles(
        @Query("minAge") minAge: Int = 40
    ): Response<List<OldMusicReportResponse>>
}

