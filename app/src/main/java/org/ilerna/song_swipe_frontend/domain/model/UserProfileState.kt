package org.ilerna.song_swipe_frontend.domain.model

/**
 * Represents the state of the user profile loading process
 * Separate from AuthState to handle profile fetching independently
 */
sealed class UserProfileState {

    /**
     * Profile has not been requested yet
     */
    data object Idle : UserProfileState()

    /**
     * Profile is being loaded from Spotify API
     */
    data object Loading : UserProfileState()

    /**
     * Profile loaded successfully
     * @param user The user profile data from Spotify
     */
    data class Success(val user: User) : UserProfileState()

    /**
     * Failed to load profile
     * @param message Error message describing the failure
     */
    data class Error(val message: String) : UserProfileState()
}