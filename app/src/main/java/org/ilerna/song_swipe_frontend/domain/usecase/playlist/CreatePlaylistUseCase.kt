package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.repository.PlaylistRepository

/**
 * Use case for creating a new Spotify playlist.
 */
class CreatePlaylistUseCase(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(
        userId: String,
        name: String,
        description: String?,
        isPublic: Boolean
    ): NetworkResult<Playlist> {
        return repository.createPlaylist(userId, name, description, isPublic)
    }
}

/**
 * Use case for adding items to an existing playlist.
 *
 * This class is currently a placeholder and does not contain any implementation.
 */
class AddItemsToPlaylistUseCase(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(
        playlistId: String,
        trackIds: List<String>
    ): NetworkResult<String> {
        return repository.addItemsToPlaylist(playlistId, trackIds)
    }
}

