package com.example.musicfilemanager.api.models

import com.google.gson.annotations.SerializedName

/**
 * Response model for Storage Report
 * GET /api/reports/storage
 */
data class StorageReportResponse(
    @SerializedName("totalFiles")
    val totalFiles: Int,

    @SerializedName("totalStorageSize")
    val totalStorageSize: Long, // in bytes

    @SerializedName("formattedStorageSize")
    val formattedStorageSize: String // e.g., "1.5 GB"
)

/**
 * Response model for Genre Report
 * GET /api/reports/by-genre
 */
data class GenreReportResponse(
    @SerializedName("genreName")
    val genreName: String,

    @SerializedName("fileCount")
    val fileCount: Int,

    @SerializedName("totalSize")
    val totalSize: Long // in bytes
)

/**
 * Response model for Year Report
 * GET /api/reports/by-year
 */
data class YearReportResponse(
    @SerializedName("year")
    val year: Int,

    @SerializedName("fileCountByYear")
    val fileCount: Int
)

/**
 * Response model for Old Music Report
 * GET /api/reports/old-music
 */
data class OldMusicReportResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("fileCode")
    val fileCode: String,

    @SerializedName("fileName")
    val fileName: String,

    @SerializedName("releaseYear")
    val releaseYear: Int,

    @SerializedName("age")
    val age: Int,

    @SerializedName("artist")
    val artist: String?,

    @SerializedName("genreName")
    val genreName: String?
)

