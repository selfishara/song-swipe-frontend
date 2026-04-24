package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import kotlinx.coroutines.flow.Flow
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Streams tracks from one or more Spotify playlists, emitting cumulative batches as each
 * playlist returns. Intended for the Swipe screen, which unveils the deck as soon as the
 * first batch arrives and grows it in the background.
 *
 * @param repository Spotify repository abstraction.
 */
class StreamPlaylistTracksUseCase(
    private val repository: SpotifyRepository
) {

    operator fun invoke(
        playlistIds: List<String>,
        maxTotal: Int = 50
    ): Flow<NetworkResult<List<Track>>> {
        require(playlistIds.isNotEmpty()) { "Playlist IDs list cannot be empty" }
        require(playlistIds.all { it.isNotBlank() }) { "All playlist IDs must be non-blank" }
        return repository.streamMultiPlaylistTracks(playlistIds, maxTotal)
    }
}
