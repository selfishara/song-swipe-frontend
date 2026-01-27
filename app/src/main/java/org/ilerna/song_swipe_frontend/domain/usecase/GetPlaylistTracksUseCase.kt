package org.ilerna.song_swipe_frontend.domain.usecase

import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.repository.PlaylistRepository

/**
 * Use case for fetching tracks of a Spotify playlist
 */
class GetPlaylistTracksUseCase(
    private val repository: PlaylistRepository
) {
    /**
     * Executes the use case to fetch playlist tracks
     *
     * @param playlistId The ID of the playlist
     * @param token The authentication token
     * @return A Result containing a list of Track objects or an error
     */
    suspend operator fun invoke(playlistId: String, token: String): Result<List<Track>> {
        // Add any business logic if needed:
        //-Track can't be empty
        //-PlaylistId must be valid format
        //-Handle specific errors
        return repository.getPlaylistTracks(playlistId, token)
    }
}
