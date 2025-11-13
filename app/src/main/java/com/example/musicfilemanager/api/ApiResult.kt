package com.example.musicfilemanager.api

/**
 * Sealed class representing API call results
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Extension function to handle API calls safely
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            // Handle Unit responses (DELETE, etc.) - body can be null
            if (body != null || response.code() == 204) {
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(body ?: Unit as T)
            } else {
                ApiResult.Error("Empty response body")
            }
        } else {
            ApiResult.Error(
                message = response.message() ?: "Unknown error",
                code = response.code()
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(
            message = e.message ?: "Network error occurred"
        )
    }
}

