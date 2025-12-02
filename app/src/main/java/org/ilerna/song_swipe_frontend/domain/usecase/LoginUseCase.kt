package org.ilerna.song_swipe_frontend.domain.usecase

import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.data.repository.AuthRepository

/**
 * Use case for handling user login with Spotify via Supabase
 */
class LoginUseCase(private val authRepository: AuthRepository) {
    
    /**
     * Initiates the Spotify login flow via Supabase
     * Supabase will automatically open the browser
     */
    suspend fun initiateLogin() {
        authRepository.initiateSpotifyLogin()
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
     * Checks if there's an active session
     */
    suspend fun hasActiveSession() = authRepository.hasActiveSession()
    
    /**
     * Waits for Supabase Auth to finish initialization
     */
    suspend fun awaitInitialization() {
        // This will be delegated to the repository
        // For now, we'll add a small delay to ensure Supabase has loaded
        kotlinx.coroutines.delay(200)
    }
    
    /**
     * Signs out the current user
     */
    suspend fun signOut() = authRepository.signOut()
}
