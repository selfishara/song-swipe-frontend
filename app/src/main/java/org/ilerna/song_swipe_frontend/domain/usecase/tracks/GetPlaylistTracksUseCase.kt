package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Retrieves tracks from one or more Spotify playlists.
 * Fetches all playlists in parallel, deduplicates, shuffles, and caps the result.
 *
 * @param repository Spotify repository abstraction.
 */
class GetPlaylistTracksUseCase(
    private val repository: SpotifyRepository
) {

    /**
     * Fetches tracks for the given playlist IDs.
     *
     * @param playlistIds List of Spotify playlist IDs to aggregate.
     * @return NetworkResult containing deduplicated, shuffled list of tracks or an error.
     */
    suspend operator fun invoke(playlistIds: List<String>): NetworkResult<List<Track>> {
        require(playlistIds.isNotEmpty()) { "Playlist IDs list cannot be empty" }
        require(playlistIds.all { it.isNotBlank() }) { "All playlist IDs must be non-blank" }
        return repository.getMultiPlaylistTracks(playlistIds)
    }
}