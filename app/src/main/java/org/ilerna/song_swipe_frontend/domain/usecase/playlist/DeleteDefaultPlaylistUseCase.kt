package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.DefaultPlaylistRepository

/**
 * Use case to delete the default playlist reference from Supabase.
 * This allows the user to "reset" the flow and create a new playlist.
 *
 * Note: The playlist will still exist on Spotify unless deleted separately.
 */
class DeleteDefaultPlaylistUseCase(
    private val defaultPlaylistRepository: DefaultPlaylistRepository
) {
    suspend operator fun invoke(userId: String): NetworkResult<Unit> {
        return defaultPlaylistRepository.deleteDefaultPlaylist(userId)
    }
}
