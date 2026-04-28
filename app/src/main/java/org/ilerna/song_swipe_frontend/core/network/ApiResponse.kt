package org.ilerna.song_swipe_frontend.core.network

import org.ilerna.song_swipe_frontend.core.network.interceptors.ForbiddenException
import org.ilerna.song_swipe_frontend.core.network.interceptors.HttpException
import org.ilerna.song_swipe_frontend.core.network.interceptors.NotFoundException
import org.ilerna.song_swipe_frontend.core.network.interceptors.ServerException
import org.ilerna.song_swipe_frontend.core.network.interceptors.TooManyRequestsException
import org.ilerna.song_swipe_frontend.core.network.interceptors.UnauthorizedException
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
         * Handles network exceptions, preserving the HTTP status code for
         * typed exceptions thrown by the interceptor chain so that downstream
         * code can react to specific failures (e.g. 401 -> forced logout).
         */
        fun <T> create(error: Throwable): ApiResponse<T> {
            val code = when (error) {
                is UnauthorizedException -> 401
                is ForbiddenException -> 403
                is NotFoundException -> 404
                is TooManyRequestsException -> 429
                is ServerException -> 500
                is HttpException -> error.code
                else -> -1
            }
            return Error(
                code = code,
                message = error.message ?: "Connection error"
            )
        }
    }
}
