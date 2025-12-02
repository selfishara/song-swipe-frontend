package org.ilerna.song_swipe_frontend.domain.usecase

import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.data.repository.AuthRepository

/**
 * Use case for handling user login with Spotify via Supabase
 */
class LoginUseCase(private val authRepository: AuthRepository) {
    
    /**
     * Initiates the Spotify login flow via Supabase
     * @return OAuth URL to open in browser
     */
    suspend fun initiateLogin(): String {
        return authRepository.initiateSpotifyLogin()
    }
    
    /**
     * Handles the authentication callback from Supabase
     * @param url The deep link URL containing session tokens
     * @return AuthState representing the result
     */
    suspend fun handleAuthResponse(url: String): AuthState {
        return authRepository.handleAuthCallback(url)
    }
    
    /**
     * Gets the current authenticated user
     */
    suspend fun getCurrentUser() = authRepository.getCurrentUser()
    
    /**
     * Signs out the current user
     */
    suspend fun signOut() = authRepository.signOut()
}
