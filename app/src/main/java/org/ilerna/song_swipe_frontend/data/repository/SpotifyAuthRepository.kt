package org.ilerna.song_swipe_frontend.data.repository

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.ilerna.song_swipe_frontend.core.config.AppConfig
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.User

/**
 * Implementation of AuthRepository using Spotify SDK
 */
class SpotifyAuthRepository(
    private val activity: Activity
) : AuthRepository {
    
    override suspend fun initiateSpotifyLogin() {
        val builder = AuthorizationRequest.Builder(
            AppConfig.SPOTIFY_CLIENT_ID,
            AuthorizationResponse.Type.CODE,
            AppConfig.SPOTIFY_REDIRECT_URI
        )

        builder.setScopes(AppConfig.SPOTIFY_SCOPES)
        val request = builder.build()

        // Use browser-based login instead of WebView
        // This allows Google/Facebook login and avoids WebView issues
        AuthorizationClient.openLoginInBrowser(activity, request)
    }

    override suspend fun handleAuthCallback(url: String): AuthState {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUser(): User? {
        TODO("Not yet implemented")
    }

    override suspend fun getSpotifyAccessToken(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun signOut() {
        TODO("Not yet implemented")
    }

    override suspend fun hasActiveSession(): Boolean {
        TODO("Not yet implemented")
    }

    suspend fun handleAuthCallback(uri: Uri): AuthState {
        Log.d(AppConfig.LOG_TAG, "Received callback URI: $uri")
        val response = AuthorizationResponse.fromUri(uri)

        return when (response.type) {
            AuthorizationResponse.Type.CODE -> {
                Log.d(AppConfig.LOG_TAG, "Authorization successful! Code: ${response.code}")
                AuthState.Success(response.code)
            }
            AuthorizationResponse.Type.ERROR -> {
                Log.e(AppConfig.LOG_TAG, "Authorization error: ${response.error}")
                AuthState.Error("Error: ${response.error}")
            }
            else -> {
                Log.d(AppConfig.LOG_TAG, "Authorization cancelled")
                AuthState.Error("Authorization cancelled")
            }
        }
    }
}
