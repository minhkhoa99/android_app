package com.example.musicfilemanager.api

import com.example.musicfilemanager.api.models.MusicFileRequest
import com.example.musicfilemanager.api.models.MusicFileResponse
import com.example.musicfilemanager.api.models.PagedResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service for Music Files endpoints
 * Base URL: http://10.0.2.2:3005/api
 */
interface MusicApiService {

    /**
     * Get all music files with pagination
     * GET /api/music-files?page=0&size=20&sort=createdAt,desc
     */
    @GET("music-files")
    suspend fun getAllMusicFiles(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<PagedResponse<MusicFileResponse>>

    /**
     * Get music file by ID
     * GET /api/music-files/{id}
     */
    @GET("music-files/{id}")
    suspend fun getMusicFileById(@Path("id") id: Int): Response<MusicFileResponse>

    /**
     * Get music file by code
     * GET /api/music-files/code/{code}
     */
    @GET("music-files/code/{code}")
    suspend fun getMusicFileByCode(@Path("code") code: String): Response<MusicFileResponse>

    /**
     * Search music files by keyword
     * GET /api/music-files/search?keyword={keyword}&page=0&size=20
     */
    @GET("music-files/search")
    suspend fun searchMusicFiles(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<PagedResponse<MusicFileResponse>>

    /**
     * Filter music files by genre
     * GET /api/music-files/filter/genre/{genreId}
     */
    @GET("music-files/filter/genre/{genreId}")
    suspend fun filterByGenre(
        @Path("genreId") genreId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<PagedResponse<MusicFileResponse>>

    /**
     * Filter music files by year
     * GET /api/music-files/filter/year/{year}
     */
    @GET("music-files/filter/year/{year}")
    suspend fun filterByYear(
        @Path("year") year: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<PagedResponse<MusicFileResponse>>

    /**
     * Filter music files for age 40+
     * GET /api/music-files/filter/for-age-40-plus
     * Returns array directly, not PagedResponse
     */
    @GET("music-files/filter/for-age-40-plus")
    suspend fun getForAge40Plus(): Response<List<MusicFileResponse>>

    /**
     * Create new music file
     * POST /api/music-files
     */
    @POST("music-files")
    suspend fun createMusicFile(@Body request: MusicFileRequest): Response<MusicFileResponse>

    /**
     * Update music file
     * PUT /api/music-files/{id}
     */
    @PUT("music-files/{id}")
    suspend fun updateMusicFile(
        @Path("id") id: Int,
        @Body request: MusicFileRequest
    ): Response<MusicFileResponse>

    /**
     * Delete music file
     * DELETE /api/music-files/{id}
     */
    @DELETE("music-files/{id}")
    suspend fun deleteMusicFile(@Path("id") id: Int): Response<Unit>

    /**
     * Upload music file (step 1)
     * POST /api/music-files/upload
     * Gửi fileCode tạm (server required), nhưng server sẽ trả về fileCode chính thức
     * Returns: { id, fileCode, fileName, filePath, downloadLink, ... }
     */
    @Multipart
    @POST("music-files/upload")
    suspend fun uploadMusicFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("fileCode") fileCode: okhttp3.RequestBody,
        @Part("fileName") fileName: okhttp3.RequestBody
    ): Response<MusicFileResponse>

    /**
     * Get statistics
     * GET /api/music-files/stats/summary
     */
    @GET("music-files/stats/summary")
    suspend fun getStatistics(): Response<Map<String, Any>>
}

