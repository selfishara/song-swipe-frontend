package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Removes a single track from the given Spotify playlist.
 */
class RemoveItemFromPlaylistUseCase(
    private val spotifyRepository: SpotifyRepository
) {
    suspend operator fun invoke(
        playlistId: String,
        trackId: String
    ): NetworkResult<String> {
        return spotifyRepository.removeItemsFromPlaylist(
            playlistId = playlistId,
            trackIds = listOf(trackId)
        )
    }
}
