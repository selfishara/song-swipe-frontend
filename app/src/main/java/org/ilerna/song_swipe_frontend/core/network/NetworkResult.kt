package org.ilerna.song_swipe_frontend.core.network

/**
 * Sealed class that wraps network operation responses, allowing to handle
 * three possible states: success, error, and loading.
 * 
 * This is the standard return type for all functions that make HTTP calls
 * (Retrofit) or access remote data.
 */
sealed class NetworkResult<out T> {
    /**
     * Successful operation, contains data of type T
     */
    data class Success<T>(val data: T) : NetworkResult<T>()
    
    /**
     * Failed operation, includes message and optional HTTP code
     */
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    
    /**
     * Operation in progress (useful for showing loading indicators)
     */
    data object Loading : NetworkResult<Nothing>()
}
