package org.ilerna.song_swipe_frontend.domain.repository

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.User

/**
 * Repository interface for Spotify API operations
 * Handles user profile retrieval from Spotify Web API
 */
interface SpotifyRepository {

    /**
     * Gets the current user's Spotify profile
     * Requires a valid Spotify access token from the authenticated session
     *
     * @return NetworkResult containing User profile data or error
     */
    suspend fun getCurrentUserProfile(): NetworkResult<User>
}