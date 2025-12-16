package org.ilerna.song_swipe_frontend.core.network.interceptors

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that automatically injects the Spotify authentication token
 * into all HTTP requests to the Spotify API.
 * 
 * This interceptor runs before each request, adding the Authorization header
 * (Bearer token) needed to authenticate with Spotify Web API.
 * 
 * @param supabaseClient Supabase client instance to retrieve the provider token
 */
class SpotifyAuthInterceptor(
    private val supabaseClient: SupabaseClient
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get Spotify token from Supabase
        val spotifyToken = getSpotifyToken()
        
        // If no token available, proceed without modifying the request
        if (spotifyToken.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // Add authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $spotifyToken")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
    
    /**
     * Retrieves the Spotify provider token from Supabase session
     */
    private fun getSpotifyToken(): String? {
        return try {
            // Get provider_token (Spotify token) from Supabase
            // Using runBlocking since interceptor is synchronous but Supabase is suspend
            runBlocking {
                supabaseClient.auth.currentSessionOrNull()?.providerToken
            }
        } catch (e: Exception) {
            null
        }
    }
}
