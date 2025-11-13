package com.example.musicfilemanager.api

import com.example.musicfilemanager.api.models.GenreRequest
import com.example.musicfilemanager.api.models.GenreResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Genre API Service Interface
 * Base URL: {{base_url}}/api/genres
 */
interface GenreApiService {

    /**
     * Get all genres
     * GET /api/genres
     */
    @GET("genres")
    suspend fun getAllGenres(): Response<List<GenreResponse>>

    /**
     * Get genre by ID
     * GET /api/genres/{id}
     */
    @GET("genres/{id}")
    suspend fun getGenreById(@Path("id") id: Int): Response<GenreResponse>

    /**
     * Get genre by code
     * GET /api/genres/code/{genreCode}
     */
    @GET("genres/code/{genreCode}")
    suspend fun getGenreByCode(@Path("genreCode") genreCode: String): Response<GenreResponse>

    /**
     * Create new genre
     * POST /api/genres
     */
    @POST("genres")
    suspend fun createGenre(@Body request: GenreRequest): Response<GenreResponse>

    /**
     * Update genre
     * PUT /api/genres/{id}
     */
    @PUT("genres/{id}")
    suspend fun updateGenre(
        @Path("id") id: Int,
        @Body request: GenreRequest
    ): Response<GenreResponse>

    /**
     * Delete genre
     * DELETE /api/genres/{id}
     */
    @DELETE("genres/{id}")
    suspend fun deleteGenre(@Path("id") id: Int): Response<Unit>
}

