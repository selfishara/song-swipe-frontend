package org.ilerna.song_swipe_frontend.data.repository.impl

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Spotify
import kotlinx.coroutines.delay
import org.ilerna.song_swipe_frontend.core.config.AppConfig
import org.ilerna.song_swipe_frontend.core.config.SupabaseConfig
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.repository.AuthRepository

/**
 * Implementation of AuthRepository using Supabase Auth
 * Handles Spotify OAuth flow through Supabase
 * @param supabaseClient The Supabase client instance (defaults to singleton for production use)
 */
class SupabaseAuthRepository(
    private val supabaseClient: SupabaseClient = SupabaseConfig.client
) : AuthRepository {

    private val supabase = supabaseClient

    override suspend fun initiateSpotifyLogin() {
        try {
            // Supabase will automatically open the browser for OAuth
            supabase.auth.signInWith(Spotify)
        } catch (e: Exception) {
            Log.e(AppConfig.LOG_TAG, "Error initiating Spotify login", e)
            throw e
        }
    }

    override suspend fun handleAuthCallback(url: String): AuthState {
        return try {
            Log.d(AppConfig.LOG_TAG, "Processing auth callback: $url")

            // Extract tokens from URL fragment
            // Format: songswipe://callback#access_token=...&refresh_token=...
            val fragment = url.substringAfter("#", "")
            val params = fragment.split("&").mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }.toMap()

            val accessToken = params["access_token"]
            val refreshToken = params["refresh_token"]

            if (accessToken != null && refreshToken != null) {
                // Import the session using auth tokens
                // This is a suspend function that completes asynchronously
                supabase.auth.importAuthToken(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    retrieveUser = true,
                    autoRefresh = true
                )

                // Poll for session with timeout
                // importAuthToken returns immediately but the user retrieval happens async
                var session = supabase.auth.currentSessionOrNull()
                var attempts = 0
                val maxAttempts = 20 // 2 seconds max

                while (session == null && attempts < maxAttempts) {
                    delay(100)
                    session = supabase.auth.currentSessionOrNull()
                    attempts++
                }

                if (session != null) {
                    Log.d(AppConfig.LOG_TAG, "Session imported successfully after ${attempts * 100}ms")
                    Log.d(AppConfig.LOG_TAG, "User ID: ${session.user?.id}")
                    Log.d(AppConfig.LOG_TAG, "User email: ${session.user?.email}")
                    AuthState.Success(session.user?.id ?: "")
                } else {
                    Log.e(AppConfig.LOG_TAG, "Failed to import session after ${attempts * 100}ms")
                    AuthState.Error("Failed to establish session")
                }
            } else {
                Log.e(AppConfig.LOG_TAG, "Missing tokens in callback URL")
                AuthState.Error("Invalid authentication response")
            }
        } catch (e: Exception) {
            Log.e(AppConfig.LOG_TAG, "Auth callback error: ${e.message}", e)
            AuthState.Error(e.message ?: "Authentication failed")
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            supabase.auth.currentUserOrNull()?.let { user ->
                User(
                    id = user.id,
                    email = user.email ?: "",
                    displayName = user.userMetadata?.get("name")?.toString()?.removeSurrounding("\"") ?: "",
                    profileImageUrl = user.userMetadata?.get("avatar_url")?.toString()?.removeSurrounding("\""),
                    spotifyId = user.userMetadata?.get("provider_id")?.toString()?.removeSurrounding("\"")
                )
            }
        } catch (e: Exception) {
            Log.e(AppConfig.LOG_TAG, "Error getting current user", e)
            null
        }
    }

    override suspend fun getSpotifyAccessToken(): String? {
        return try {
            // Get the provider token (Spotify access token) from the session
            supabase.auth.currentSessionOrNull()?.providerToken
        } catch (e: Exception) {
            Log.e(AppConfig.LOG_TAG, "Error getting Spotify token", e)
            null
        }
    }

    override suspend fun signOut() {
        try {
            supabase.auth.signOut()
            Log.d(AppConfig.LOG_TAG, "User signed out successfully")
        } catch (e: Exception) {
            Log.e(AppConfig.LOG_TAG, "Error signing out", e)
        }
    }

    override suspend fun hasActiveSession(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }
}