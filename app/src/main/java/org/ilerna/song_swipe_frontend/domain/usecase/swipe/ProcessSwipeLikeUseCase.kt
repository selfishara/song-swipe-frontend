package org.ilerna.song_swipe_frontend.domain.usecase.swipe

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.AddItemToDefaultPlaylistUseCase

/**
 * Executes the swipe-like flow to add a track to the user's default playlist.
 *
 * This use case orchestrates the process by:
 * - Calling AddItemToDefaultPlaylistUseCase
 * - Retrying the operation up to 3 times in case of failure
 *
 * @param supabaseUserId The ID of the authenticated user in Supabase
 * @param spotifyUserId The ID of the user in Spotify
 * @param trackId The ID of the track to be added
 *
 * @return Result containing success confirmation or an error after retries
 */

class ProcessSwipeLikeUseCase(
    private val addItemToDefaultPlaylistUseCase: AddItemToDefaultPlaylistUseCase
) {
    suspend fun handle(
        supabaseUserId: String,
        spotifyUserId: String,
        trackId: String
    ): NetworkResult<String> {

        repeat(3) { attempt ->
            val result = addItemToDefaultPlaylistUseCase(
                supabaseUserId,
                spotifyUserId,
                trackId
            )

            if (result is NetworkResult.Success) return result

            if (attempt == 2) return result
        }

        return NetworkResult.Error("Unknown error")
    }
}