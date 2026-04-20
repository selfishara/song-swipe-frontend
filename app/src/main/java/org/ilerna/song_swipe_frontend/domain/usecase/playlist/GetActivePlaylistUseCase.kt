package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import kotlinx.coroutines.flow.Flow
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ActivePlaylistDataStore

/**
 * Exposes the currently active playlist via reactive flows so UI state stays
 * in sync with DataStore changes.
 */
class GetActivePlaylistUseCase(
    private val activePlaylistDataStore: ActivePlaylistDataStore
) {
    fun id(): Flow<String?> = activePlaylistDataStore.activePlaylistId

    fun name(): Flow<String?> = activePlaylistDataStore.activePlaylistName

    suspend fun idSync(): String? = activePlaylistDataStore.getActivePlaylistIdSync()

    suspend fun nameSync(): String? = activePlaylistDataStore.getActivePlaylistNameSync()
}
