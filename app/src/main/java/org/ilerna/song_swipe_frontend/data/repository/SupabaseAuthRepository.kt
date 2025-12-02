package org.ilerna.song_swipe_frontend.data.repository

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Spotify
import org.ilerna.song_swipe_frontend.core.config.AppConfig
import org.ilerna.song_swipe_frontend.core.config.SupabaseConfig
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.User

/**
 * Implementation of AuthRepository using Supabase Auth
 * Handles Spotify OAuth flow through Supabase
 */
class SupabaseAuthRepository : AuthRepository {
    
    private val supabase = SupabaseConfig.client
    
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
            val fragment = url.substringAfter("#")
            val params = fragment.split("&").associate {
                val (key, value) = it.split("=")
                key to value
            }
            
            val accessToken = params["access_token"]
            val refreshToken = params["refresh_token"]
            
            if (accessToken != null && refreshToken != null) {
                // Import the session using auth tokens
                supabase.auth.importAuthToken(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    retrieveUser = true,
                    autoRefresh = true
                )
                
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    Log.d(AppConfig.LOG_TAG, "Session imported successfully")
                    Log.d(AppConfig.LOG_TAG, "User ID: ${session.user?.id}")
                    Log.d(AppConfig.LOG_TAG, "User email: ${session.user?.email}")
                    AuthState.Success(session.user?.id ?: "")
                } else {
                    Log.e(AppConfig.LOG_TAG, "Failed to import session")
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
            val user = supabase.auth.currentUserOrNull()
            user?.let {
                User(
                    id = it.id,
                    email = it.email ?: "",
                    displayName = it.userMetadata?.get("name") as? String ?: "",
                    profileImageUrl = it.userMetadata?.get("avatar_url") as? String,
                    spotifyId = it.userMetadata?.get("provider_id") as? String
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
