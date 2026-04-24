package org.ilerna.song_swipe_frontend.data.datasource.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.activePlaylistDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "active_playlist"
)

/**
 * Persists the user's active playlist selection so it survives app restarts.
 * The active playlist is the target for liked songs during swipe sessions.
 */
class ActivePlaylistDataStore(private val context: Context) {

    companion object {
        private val PLAYLIST_ID_KEY = stringPreferencesKey("active_playlist_id")
        private val PLAYLIST_NAME_KEY = stringPreferencesKey("active_playlist_name")
    }

    val activePlaylistId: Flow<String?> = context.activePlaylistDataStore.data.map { it[PLAYLIST_ID_KEY] }

    val activePlaylistName: Flow<String?> = context.activePlaylistDataStore.data.map { it[PLAYLIST_NAME_KEY] }

    suspend fun getActivePlaylistIdSync(): String? = activePlaylistId.first()

    suspend fun getActivePlaylistNameSync(): String? = activePlaylistName.first()

    suspend fun saveActivePlaylist(playlistId: String, playlistName: String) {
        context.activePlaylistDataStore.edit { prefs ->
            prefs[PLAYLIST_ID_KEY] = playlistId
            prefs[PLAYLIST_NAME_KEY] = playlistName
        }
    }

    suspend fun clear() {
        context.activePlaylistDataStore.edit { it.clear() }
    }
}
