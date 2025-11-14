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
                ApiResult.Error("Phản hồi từ server không có dữ liệu")
            }
        } else {
            // Try to parse error body for more details
            val errorBody = response.errorBody()?.string()
            val errorMessage = when {
                errorBody != null && errorBody.isNotEmpty() -> {
                    try {
                        // Try to extract error message from JSON
                        val regex = """"message"\s*:\s*"([^"]*)"""".toRegex()
                        regex.find(errorBody)?.groupValues?.get(1) ?: errorBody.take(100)
                    } catch (_: Exception) {
                        errorBody.take(100)
                    }
                }
                response.code() == 400 -> "Dữ liệu không hợp lệ"
                response.code() == 404 -> "Không tìm thấy thông tin"
                response.code() == 409 -> "Thể loại đã tồn tại"
                response.code() == 500 -> "Lỗi server, vui lòng thử lại sau"
                else -> response.message() ?: "Lỗi không xác định"
            }

            ApiResult.Error(
                message = errorMessage,
                code = response.code()
            )
        }
    } catch (_: java.net.ConnectException) {
        ApiResult.Error(message = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.")
    } catch (_: java.net.SocketTimeoutException) {
        ApiResult.Error(message = "Timeout: Server không phản hồi. Vui lòng thử lại.")
    } catch (_: java.net.UnknownHostException) {
        ApiResult.Error(message = "Không tìm thấy server. Vui lòng kiểm tra địa chỉ API.")
    } catch (e: Exception) {
        ApiResult.Error(
            message = e.message ?: "Lỗi mạng xảy ra"
        )
    }
}

