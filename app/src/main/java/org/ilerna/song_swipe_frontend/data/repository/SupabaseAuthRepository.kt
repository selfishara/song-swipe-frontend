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
    
    override suspend fun initiateSpotifyLogin(): String {
        return try {
            // Generate OAuth URL for Spotify via Supabase
            supabase.auth.signInWith(Spotify)
            // Note: In a real implementation, this should return the URL
            // For now, this will open the browser automatically
            ""
        } catch (e: Exception) {
            Log.e(AppConfig.LOG_TAG, "Error initiating Spotify login", e)
            throw e
        }
    }
    
    override suspend fun handleAuthCallback(url: String): AuthState {
        return try {
            Log.d(AppConfig.LOG_TAG, "Processing auth callback: $url")
            
            // Parse and import session from deep link
            // Supabase will automatically handle the session tokens from the URL
            supabase.auth.parseFragmentAndImportSession(url)
            
            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                Log.d(AppConfig.LOG_TAG, "Session imported successfully")
                AuthState.Success(session.user?.id ?: "")
            } else {
                Log.e(AppConfig.LOG_TAG, "Failed to import session")
                AuthState.Error("Failed to establish session")
            }
        } catch (e: Exception) {
            Log.e(AppConfig.LOG_TAG, "Auth callback error", e)
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
