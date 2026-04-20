package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Retrieves every playlist by the current Spotify user.
 */
class GetUserPlaylistsUseCase(
    private val spotifyRepository: SpotifyRepository
) {
    suspend operator fun invoke(): NetworkResult<List<Playlist>> {
        return spotifyRepository.getUserPlaylists()
    }
}
