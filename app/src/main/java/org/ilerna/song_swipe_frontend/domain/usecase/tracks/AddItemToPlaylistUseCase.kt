package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Adds a single track to the given Spotify playlist.
 */
class AddItemToPlaylistUseCase(
    private val spotifyRepository: SpotifyRepository
) {
    suspend operator fun invoke(
        playlistId: String,
        trackId: String
    ): NetworkResult<String> {
        return spotifyRepository.addItemsToPlaylist(
            playlistId = playlistId,
            trackIds = listOf(trackId)
        )
    }
}
