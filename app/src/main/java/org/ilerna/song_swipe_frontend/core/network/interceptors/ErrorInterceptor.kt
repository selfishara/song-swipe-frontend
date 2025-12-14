package org.ilerna.song_swipe_frontend.core.network.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

/**
 * OkHttp interceptor that centralizes HTTP error handling, transforming
 * error codes into custom exceptions or logging.
 * 
 * Runs after each HTTP response, allowing to intercept and process errors
 * consistently before they reach the repositories.
 */
class ErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        return try {
            val response = chain.proceed(request)
            
            // If response is not successful, log the error
            if (!response.isSuccessful) {
                handleErrorResponse(response)
            }
            
            response
        } catch (e: NetworkException) {
            // Re-throw our custom HTTP exceptions (401, 403, 404, etc.)
            throw e
        } catch (e: java.io.IOException) {
            // Network errors (timeout, no connection, etc.)
            Log.e(TAG, "Network error: ${request.url}", e)
            throw NetworkException("Connection error: ${e.message}")
        }
    }
    
    /**
     * Handles error responses and logs them
     */
    private fun handleErrorResponse(response: Response) {
        val errorBody = response.body.string()
        val errorMessage = parseErrorMessage(errorBody, response.code)
        
        Log.e(
            TAG,
            """HTTP ${response.code} - ${response.message}
               |URL: ${response.request.url}
               |Error: $errorMessage
            """.trimMargin()
        )
        
        // Throw exception based on error code
        when (response.code) {
            401 -> throw UnauthorizedException("Invalid or expired token")
            403 -> throw ForbiddenException("You don't have permission for this action")
            404 -> throw NotFoundException("Resource not found")
            429 -> throw TooManyRequestsException("Too many requests")
            in 500..599 -> throw ServerException("Server error: $errorMessage")
            else -> throw HttpException(response.code, errorMessage)
        }
    }
    
    /**
     * Parses error message from response body (Spotify API format)
     */
    private fun parseErrorMessage(errorBody: String?, code: Int): String {
        return try {
            if (errorBody != null) {
                // Parse Spotify error JSON format
                val json = JSONObject(errorBody)
                json.getJSONObject("error").getString("message")
            } else {
                "HTTP error $code"
            }
        } catch (e: Exception) {
            errorBody ?: "Unknown error"
        }
    }
    
    companion object {
        private const val TAG = "ErrorInterceptor"
    }
}

// Custom Network Exceptions

/**
 * Base exception for network errors
 */
open class NetworkException(message: String) : Exception(message)

/**
 * Generic HTTP error
 */
class HttpException(val code: Int, message: String) : NetworkException("HTTP $code: $message")

/**
 * 401 - Invalid or expired token
 */
class UnauthorizedException(message: String) : NetworkException(message)

/**
 * 403 - No permissions
 */
class ForbiddenException(message: String) : NetworkException(message)

/**
 * 404 - Resource not found
 */
class NotFoundException(message: String) : NetworkException(message)

/**
 * 429 - Rate limit exceeded
 */
class TooManyRequestsException(message: String) : NetworkException(message)

/**
 * 500+ - Server error
 */
class ServerException(message: String) : NetworkException(message)
