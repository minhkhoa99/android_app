package com.example.musicfilemanager.api.models

import com.google.gson.annotations.SerializedName

/**
 * Response model for Genre API
 */
data class GenreResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("genreCode")
    val genreCode: String,

    @SerializedName("genreName")
    val genreName: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("ageRange")
    val ageRange: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("totalFiles")
    val totalFiles: Int = 0
)

/**
 * Request model for creating/updating Genre
 */
data class GenreRequest(
    @SerializedName("genreCode")
    val genreCode: String,

    @SerializedName("genreName")
    val genreName: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("ageRange")
    val ageRange: String? = null
)

