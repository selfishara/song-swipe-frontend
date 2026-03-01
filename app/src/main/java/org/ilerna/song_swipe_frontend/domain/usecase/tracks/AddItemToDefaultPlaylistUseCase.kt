package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase

class AddItemToDefaultPlaylistUseCase(
    private val getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase,
    private val spotifyRepository: SpotifyRepository
) {
    suspend operator fun invoke(
        supabaseUserId: String,
        spotifyUserId: String,
        trackId: String
    ): NetworkResult<String> {

        val playlistResult = getOrCreateDefaultPlaylistUseCase(
            supabaseUserId = supabaseUserId,
            spotifyUserId = spotifyUserId
        )

        if (playlistResult is NetworkResult.Error) return playlistResult

        val playlistId = (playlistResult as NetworkResult.Success).data.id

        return spotifyRepository.addItemsToPlaylist(
            playlistId = playlistId,
            trackIds = listOf(trackId)
        )
    }
}