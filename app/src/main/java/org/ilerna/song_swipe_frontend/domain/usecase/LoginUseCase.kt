package org.ilerna.song_swipe_frontend.domain.usecase

import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.data.repository.AuthRepository

/**
 * Use case for handling user login with Spotify
 */
class LoginUseCase(private val authRepository: AuthRepository) {
    
    /**
     * Initiates the Spotify login flow
     */
    suspend fun initiateLogin() {
        authRepository.initiateSpotifyLogin()
    }
    
    /**
     * Handles the authentication response from Spotify
     * @param uri The callback URI containing the response
     * @return AuthState representing the result
     */
    suspend fun handleAuthResponse(uri: android.net.Uri): AuthState {
        return authRepository.handleAuthCallback(uri)
    }
}
