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

/**
 * Extension property to create DataStore instance for Spotify tokens
 * Uses a separate file from settings to keep concerns separated
 */
private val Context.spotifyTokenDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "spotify_tokens"
)

/**
 * Interface for Spotify token data store operations
 * Allows for easier testing and dependency injection
 */
interface ISpotifyTokenDataStore {
    val accessToken: Flow<String?>
    val refreshToken: Flow<String?>
    suspend fun setTokens(accessToken: String?, refreshToken: String?)
    suspend fun getAccessTokenSync(): String?
    suspend fun getRefreshTokenSync(): String?
    suspend fun hasToken(): Boolean
    suspend fun clear()
}

/**
 * SpotifyTokenDataStore - Manages Spotify OAuth tokens using DataStore Preferences
 *
 * Provides persistent storage for Spotify access and refresh tokens.
 * Tokens are persisted across app restarts, solving the issue where
 * tokens were previously lost when stored only in memory.
 *
 * TODO: DataStore itself does not encrypt data at rest.
 *
 * @see <a href="https://developer.android.com/topic/security/data">Android Security Best Practices</a>
 */
open class SpotifyTokenDataStore(private val context: Context) : ISpotifyTokenDataStore {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("spotify_access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("spotify_refresh_token")
    }

    /**
     * Flow that emits the current Spotify access token
     */
    override val accessToken: Flow<String?> =
        context.spotifyTokenDataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }

    /**
     * Flow that emits the current Spotify refresh token
     */
    override val refreshToken: Flow<String?> =
        context.spotifyTokenDataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }

    /**
     * Stores the Spotify tokens extracted from OAuth callback
     * @param accessToken The Spotify access token
     * @param refreshToken The Spotify refresh token
     */
    override suspend fun setTokens(accessToken: String?, refreshToken: String?) {
        context.spotifyTokenDataStore.edit { preferences ->
            if (accessToken != null) {
                preferences[ACCESS_TOKEN_KEY] = accessToken
            } else {
                preferences.remove(ACCESS_TOKEN_KEY)
            }
            if (refreshToken != null) {
                preferences[REFRESH_TOKEN_KEY] = refreshToken
            } else {
                preferences.remove(REFRESH_TOKEN_KEY)
            }
        }
    }

    /**
     * Gets the current Spotify access token synchronously
     * @return The Spotify access token or null if not available
     */
    override suspend fun getAccessTokenSync(): String? = accessToken.first()

    /**
     * Gets the current Spotify refresh token synchronously
     * @return The Spotify refresh token or null if not available
     */
    override suspend fun getRefreshTokenSync(): String? = refreshToken.first()

    /**
     * Checks if a valid Spotify token is available
     * @return true if an access token exists and is not empty
     */
    override suspend fun hasToken(): Boolean = !getAccessTokenSync().isNullOrEmpty()

    /**
     * Clears all stored tokens (call on logout)
     */
    override suspend fun clear() {
        context.spotifyTokenDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }
}
