package org.ilerna.song_swipe_frontend.data.datasource.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.swipeSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "swipe_session"
)

/**
 * Persists the active swipe session so the user can resume where they left off.
 * Stores: playlistId, genre name, and current song index.
 */
class SwipeSessionDataStore(private val context: Context) {

    companion object {
        private val PLAYLIST_ID_KEY = stringPreferencesKey("session_playlist_id")
        private val GENRE_KEY = stringPreferencesKey("session_genre")
        private val CURRENT_INDEX_KEY = intPreferencesKey("session_current_index")
    }

    val playlistId: Flow<String?> = context.swipeSessionDataStore.data.map { it[PLAYLIST_ID_KEY] }
    val genre: Flow<String?> = context.swipeSessionDataStore.data.map { it[GENRE_KEY] }
    val currentIndex: Flow<Int> = context.swipeSessionDataStore.data.map { it[CURRENT_INDEX_KEY] ?: 0 }

    suspend fun getPlaylistIdSync(): String? = playlistId.first()
    suspend fun getGenreSync(): String? = genre.first()
    suspend fun getCurrentIndexSync(): Int = currentIndex.first()

    suspend fun saveSession(playlistId: String, genre: String, currentIndex: Int) {
        context.swipeSessionDataStore.edit { prefs ->
            prefs[PLAYLIST_ID_KEY] = playlistId
            prefs[GENRE_KEY] = genre
            prefs[CURRENT_INDEX_KEY] = currentIndex
        }
    }

    suspend fun saveCurrentIndex(index: Int) {
        context.swipeSessionDataStore.edit { prefs ->
            prefs[CURRENT_INDEX_KEY] = index
        }
    }

    suspend fun clearSession() {
        context.swipeSessionDataStore.edit { it.clear() }
    }
}
