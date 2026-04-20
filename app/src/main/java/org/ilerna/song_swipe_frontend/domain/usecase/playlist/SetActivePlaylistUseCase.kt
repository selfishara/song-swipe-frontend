package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ActivePlaylistDataStore

/**
 * Persists the user's active playlist selection. The active playlist is the
 * target for liked tracks produced by swiping right.
 */
class SetActivePlaylistUseCase(
    private val activePlaylistDataStore: ActivePlaylistDataStore
) {
    suspend operator fun invoke(playlistId: String, playlistName: String) {
        activePlaylistDataStore.saveActivePlaylist(
            playlistId = playlistId,
            playlistName = playlistName
        )
    }

    suspend fun clear() {
        activePlaylistDataStore.clear()
    }
}
