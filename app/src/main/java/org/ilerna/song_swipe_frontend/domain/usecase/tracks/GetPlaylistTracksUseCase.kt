package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Retrieves tracks from a Spotify playlist.
 *
 * @param repository Spotify repository abstraction.
 */
class GetPlaylistTracksUseCase(
    private val repository: SpotifyRepository
) {

    /**
     * Fetches tracks for the given playlist id.
     *
     * @param playlistId Spotify playlist id.
     * @return NetworkResult containing list of tracks or an error.
     */
    suspend operator fun invoke(playlistId: String): NetworkResult<List<Track>> {
        require(playlistId.isNotBlank()) { "Playlist ID cannot be empty" }
        return repository.getPlaylistTracks(playlistId)
    }
}