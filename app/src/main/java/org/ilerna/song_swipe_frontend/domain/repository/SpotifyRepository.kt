package org.ilerna.song_swipe_frontend.domain.repository

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
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

    /**
     * Retrieves Spotify playlists filtered by genre.
     * The genre is matched against Spotify browse categories and the resulting
     * playlists are returned as clean domain models.
     *
     * @param genre Genre or category name (e.g. "pop", "rock")
     * @return NetworkResult containing a list of Playlist or an error
     */
    suspend fun getPlaylistsByGenre(
        genre: String
    ): NetworkResult<List<Playlist>>

}