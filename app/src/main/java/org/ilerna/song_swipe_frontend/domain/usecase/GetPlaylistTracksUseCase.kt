package org.ilerna.song_swipe_frontend.domain.usecase

import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.repository.PlaylistRepository

/**
 * Use case for fetching tracks from a Spotify playlist
 *
 * @property repository The playlist repository to fetch data from
 */
class GetPlaylistTracksUseCase(
    private val repository: PlaylistRepository
) {
    /**
     * Executes the use case to fetch tracks from a playlist
     *
     * @param playlistId The ID of the Spotify playlist
     * @return Result containing a list of playable Track objects or an error
     */
    suspend operator fun invoke(playlistId: String): Result<List<Track>> {
        if (playlistId.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Playlist ID cannot be empty")
            )
        }

        return repository.getPlaylistTracks(playlistId)
            .mapCatching { tracks ->
                tracks.filter { it.is_playable }
            }
            .recoverCatching { error ->
                when {
                    error.message?.contains("401") == true ->
                        throw IllegalStateException("Authentication token expired", error)

                    error.message?.contains("404") == true ->
                        throw IllegalArgumentException("Playlist not found: $playlistId", error)

                    error.message?.contains("403") == true ->
                        throw SecurityException("No permission to access playlist", error)

                    else -> throw error
                }
            }
    }
}
