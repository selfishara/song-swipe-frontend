package org.ilerna.song_swipe_frontend.domain.model

/**
 * Represents the authentication state of the user
 */
sealed class AuthState {
    /**
     * User is not authenticated
     */
    data object Idle : AuthState()
    
    /**
     * Authentication is in progress
     */
    data object Loading : AuthState()
    
    /**
     * User successfully authenticated
     * @param authorizationCode The authorization code received from Spotify
     */
    data class Success(val authorizationCode: String) : AuthState()
    
    /**
     * Authentication failed
     * @param message Error message describing the failure
     */
    data class Error(val message: String) : AuthState()
}
