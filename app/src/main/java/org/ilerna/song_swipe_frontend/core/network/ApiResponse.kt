package org.ilerna.song_swipe_frontend.core.network

import retrofit2.Response

/**
 * Wrapper class that encapsulates Retrofit HTTP responses (Response<T>),
 * facilitating consistent handling of successful responses and errors.
 * 
 * Used in the data layer (repositories and datasources) as an intermediate
 * step before converting to NetworkResult.
 */
sealed class ApiResponse<out T> {
    /**
     * Successful HTTP response with data
     */
    data class Success<T>(val data: T) : ApiResponse<T>()
    
    /**
     * Failed HTTP response with error details
     */
    data class Error(
        val code: Int,
        val message: String,
        val errorBody: String? = null
    ) : ApiResponse<Nothing>()
    
    companion object {
        /**
         * Converts Retrofit Response<T> to ApiResponse<T>
         */
        fun <T> create(response: Response<T>): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Success(body)
                } else {
                    Error(
                        code = response.code(),
                        message = "Empty response from server"
                    )
                }
            } else {
                Error(
                    code = response.code(),
                    message = response.message(),
                    errorBody = response.errorBody()?.string()
                )
            }
        }
        
        /**
         * Handles network exceptions
         */
        fun <T> create(error: Throwable): ApiResponse<T> {
            return Error(
                code = -1,
                message = error.message ?: "Connection error"
            )
        }
    }
}
