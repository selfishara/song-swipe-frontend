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

private val Context.swipeSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "swipe_session"
)

/**
 * Persists the active swipe session so the user can resume where they left off.
 * Stores only the genre name — tracks are re-fetched and re-shuffled on resume
 * for a fresh experience each time.
 */
class SwipeSessionDataStore(private val context: Context) {

    companion object {
        private val GENRE_KEY = stringPreferencesKey("session_genre")
    }

    val genre: Flow<String?> = context.swipeSessionDataStore.data.map { it[GENRE_KEY] }

    suspend fun getGenreSync(): String? = genre.first()

    suspend fun saveGenre(genre: String) {
        context.swipeSessionDataStore.edit { prefs ->
            prefs[GENRE_KEY] = genre
        }
    }

    suspend fun clearSession() {
        context.swipeSessionDataStore.edit { it.clear() }
    }
}
