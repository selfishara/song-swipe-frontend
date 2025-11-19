package org.ilerna.song_swipe_frontend.data.repository

import android.net.Uri
import org.ilerna.song_swipe_frontend.domain.model.AuthState

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    
    /**
     * Initiates the Spotify login flow
     */
    suspend fun initiateSpotifyLogin()
    
    /**
     * Handles the authentication callback from Spotify
     * @param uri The callback URI containing the response
     * @return AuthState representing the result
     */
    suspend fun handleAuthCallback(uri: Uri): AuthState
}
