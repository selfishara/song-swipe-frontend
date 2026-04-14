package org.ilerna.song_swipe_frontend.domain.repository

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Track
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

    suspend fun getPlaylistTracks(playlistId: String): NetworkResult<List<Track>>

    /**
     * Fetches tracks from multiple playlists in parallel, then deduplicates, shuffles,
     * and returns up to [GenrePlaylistProvider.DEFAULT_SET_SIZE] tracks.
     *
     * @param playlistIds List of Spotify playlist IDs to aggregate
     * @return NetworkResult containing the aggregated, shuffled list of tracks or an error
     */
    suspend fun getMultiPlaylistTracks(playlistIds: List<String>): NetworkResult<List<Track>>

    /**
     * Adds tracks to an existing playlist.
     *
     * @param playlistId The Spotify ID of the playlist
     * @param trackIds A list of Spotify track IDs to add
     * @return NetworkResult containing the snapshot ID or an error
     */
    suspend fun addItemsToPlaylist(
        playlistId: String,
        trackIds: List<String>
    ): NetworkResult<String>

    /**
     * Removes tracks from an existing playlist.
     *
     * @param playlistId The Spotify ID of the playlist
     * @param trackIds A list of Spotify track IDs to remove
     * @return NetworkResult containing the snapshot ID or an error
     */
    suspend fun removeItemsFromPlaylist(
        playlistId: String,
        trackIds: List<String>
    ): NetworkResult<String>

}