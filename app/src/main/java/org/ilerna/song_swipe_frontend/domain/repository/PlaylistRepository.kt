package org.ilerna.song_swipe_frontend.domain.repository

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.model.Track

interface PlaylistRepository {
    /**
     * Fetches the tracks of a playlist by its ID
     *
     * @param playlistId The ID of the playlist
     * @return NetworkResult containing a list of Track objects or an error
     */
    suspend fun getPlaylistTracks(playlistId: String): NetworkResult<List<Track>>

    /**
     * Creates a new playlist for the user
     *
     * @param userId The Spotify user ID
     * @param name The name of the playlist
     * @param description Optional description of the playlist
     * @param isPublic Whether the playlist should be public
     * @return NetworkResult containing the created Playlist or an error
     */
    suspend fun createPlaylist(
        userId: String,
        name: String,
        description: String?,
        isPublic: Boolean
    ): NetworkResult<Playlist>

    /**
     * Adds tracks to an existing playlist
     *
     * @param playlistId The ID of the playlist to add tracks to
     * @param trackIds A list of Spotify track IDs to add to the playlist
     * @return NetworkResult containing a success message or an error
     */
    suspend fun addItemsToPlaylist(
        playlistId: String,
        trackIds: List<String>
    ): NetworkResult<String>
}