package org.ilerna.song_swipe_frontend.core.network.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.ilerna.song_swipe_frontend.core.auth.SpotifyTokenHolder
import org.ilerna.song_swipe_frontend.core.config.AppConfig

/**
 * OkHttp interceptor that automatically injects the Spotify authentication token
 * into all HTTP requests to the Spotify API.
 *
 * This interceptor runs before each request, adding the Authorization header
 * (Bearer token) needed to authenticate with Spotify Web API.
 *
 * The token is retrieved from SpotifyTokenHolder, which stores the provider_token
 * extracted from the OAuth callback URL.
 */
class SpotifyAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get Spotify token from holder
        val spotifyToken = SpotifyTokenHolder.getAccessToken()

        // If no token available, proceed without modifying the request
        if (spotifyToken.isNullOrEmpty()) {
            Log.w(AppConfig.LOG_TAG, "SpotifyAuthInterceptor: No token available")
            return chain.proceed(originalRequest)
        }

        Log.d(AppConfig.LOG_TAG, "SpotifyAuthInterceptor: Adding Bearer token to request")

        // Add authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $spotifyToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}