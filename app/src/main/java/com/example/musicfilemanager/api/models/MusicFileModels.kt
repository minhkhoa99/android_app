package com.example.musicfilemanager.api.models

import com.google.gson.annotations.SerializedName

/**
 * Music File Request Model (for POST/PUT)
 */
data class MusicFileRequest(
    @SerializedName("fileCode")
    val fileCode: String,

    @SerializedName("fileName")
    val fileName: String,

    @SerializedName("filePath")
    val filePath: String? = null,

    @SerializedName("genreId")
    val genreId: Int,

    @SerializedName("fileType")
    val fileType: String? = null,

    @SerializedName("downloadLink")
    val downloadLink: String? = null,

    @SerializedName("artist")
    val artist: String? = null,

    @SerializedName("album")
    val album: String? = null,

    @SerializedName("releaseYear")
    val releaseYear: Int? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("duration")
    val duration: Int? = null, // in seconds

    @SerializedName("fileSize")
    val fileSize: Long? = null, // in bytes

    @SerializedName("ageRange")
    val ageRange: String? = null
)

/**
 * Music File Response Model (from GET)
 */
data class MusicFileResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("fileCode")
    val fileCode: String,

    @SerializedName("fileName")
    val fileName: String,

    @SerializedName("filePath")
    val filePath: String? = null,

    @SerializedName("thumbnailPath")
    val thumbnailPath: String? = null,

    @SerializedName("fileType")
    val fileType: String? = null,

    @SerializedName("genreId")
    val genreId: Int,

    @SerializedName("genreName")
    val genreName: String? = null,

    @SerializedName("downloadLink")
    val downloadLink: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("artist")
    val artist: String? = null,

    @SerializedName("album")
    val album: String? = null,

    @SerializedName("duration")
    val duration: Int? = null, // in seconds

    @SerializedName("fileSize")
    val fileSize: Long? = null, // in bytes

    @SerializedName("releaseYear")
    val releaseYear: Int? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("ageRange")
    val ageRange: String? = null,

    @SerializedName("age")
    val age: Int? = null // calculated field
)

/**
 * Paged Response Model (for paginated endpoints)
 */
data class PagedResponse<T>(
    @SerializedName("content")
    val content: List<T>,

    @SerializedName("totalElements")
    val totalElements: Long = 0,

    @SerializedName("totalPages")
    val totalPages: Int = 0,

    @SerializedName("size")
    val size: Int = 0,

    @SerializedName("number")
    val number: Int = 0,

    @SerializedName("numberOfElements")
    val numberOfElements: Int = 0,

    @SerializedName("first")
    val first: Boolean = true,

    @SerializedName("last")
    val last: Boolean = true,

    @SerializedName("empty")
    val empty: Boolean = false
)

