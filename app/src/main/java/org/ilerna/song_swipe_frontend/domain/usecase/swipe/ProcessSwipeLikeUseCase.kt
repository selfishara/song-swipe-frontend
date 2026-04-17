package org.ilerna.song_swipe_frontend.domain.usecase.swipe

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.AddItemToPlaylistUseCase

/**
 * Orchestrates adding a liked track to the currently active playlist.
 * Retries the add operation up to 3 times in case of transient failures.
 *
 * @param playlistId The Spotify ID of the active playlist
 * @param trackId The Spotify ID of the track to add
 */
class ProcessSwipeLikeUseCase(
    private val addItemToPlaylistUseCase: AddItemToPlaylistUseCase
) {
    suspend fun handle(
        playlistId: String,
        trackId: String
    ): NetworkResult<String> {

        repeat(3) { attempt ->
            val result = addItemToPlaylistUseCase(
                playlistId = playlistId,
                trackId = trackId
            )

            if (result is NetworkResult.Success) return result

            if (attempt == 2) return result
        }

        return NetworkResult.Error("Unknown error")
    }
}
