package org.ilerna.song_swipe_frontend.domain.repository

import org.ilerna.song_swipe_frontend.domain.model.Track

interface PlaylistRepository {
    /** Fetches the tracks of a playlist by its ID
     *
     * @param playlistId The ID of the playlist
     * @param token The authentication token
     * @return A Result containing a list of Track objects or an error
     */
    suspend fun getPlaylistTracks(playlistId: String, token: String): Result<List<Track>>
}