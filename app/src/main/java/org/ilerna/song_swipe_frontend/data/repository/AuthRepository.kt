package org.ilerna.song_swipe_frontend.data.repository

import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.User

/**
 * Repository interface for authentication operations
 * Updated to support Supabase OAuth flow
 */
interface AuthRepository {
    
    /**
     * Initiates Spotify login via Supabase OAuth
     * @return The OAuth URL to open in browser
     */
    suspend fun initiateSpotifyLogin()

    /**
     * Handles the OAuth callback and imports the session
     * @param url The deep link URL containing session tokens
     * @return AuthState representing the result
     */
    suspend fun handleAuthCallback(url: String): AuthState
    
    /**
     * Gets the current authenticated user
     * @return User if authenticated, null otherwise
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Gets the Spotify access token from the current session
     * @return Spotify access token if available, null otherwise
     */
    suspend fun getSpotifyAccessToken(): String?
    
    /**
     * Signs out the current user
     */
    suspend fun signOut()
    
    /**
     * Checks if user has an active session
     * @return true if session is active, false otherwise
     */
    suspend fun hasActiveSession(): Boolean
}
