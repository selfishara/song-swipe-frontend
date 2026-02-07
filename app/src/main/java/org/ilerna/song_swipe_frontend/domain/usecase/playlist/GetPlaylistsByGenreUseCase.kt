package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Use case for fetching Spotify playlists by genre.
 */
class GetPlaylistsByGenreUseCase(
    private val spotifyRepository: SpotifyRepository
) {
    suspend operator fun invoke(genre: String): NetworkResult<List<Playlist>> {
        if (genre.isBlank()) {
            return NetworkResult.Error(
                message = "Genre cannot be empty",
                code = null
            )
        }
        return spotifyRepository.getPlaylistsByGenre(genre)
    }
}