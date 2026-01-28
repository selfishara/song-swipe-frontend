package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

class GetPlaylistTracksUseCase(
    private val repository: SpotifyRepository
) {
    suspend operator fun invoke(playlistId: String): List<Track>? {
        return repository.getPlaylistTracks(playlistId)
    }
}