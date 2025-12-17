package org.ilerna.song_swipe_frontend.core.auth

/**
 * Thread-safe holder for the Spotify provider token
 *
 * This is necessary because Supabase's importAuthToken() does not persist
 * the provider_token (Spotify token) in the session. We need to extract it
 * from the OAuth callback URL and store it separately.
 *
 * TODO: Refactor to use EncryptedSharedPreferences or DataStore for persistence.
 *       Current implementation stores tokens in memory only - they are lost on app restart.
 *       See: https://developer.android.com/topic/security/data
 *       Priority: Medium (affects session persistence across app restarts)
 */
object SpotifyTokenHolder {

    @Volatile
    private var spotifyAccessToken: String? = null

    @Volatile
    private var spotifyRefreshToken: String? = null

    /**
     * Stores the Spotify tokens extracted from OAuth callback
     */
    fun setTokens(accessToken: String?, refreshToken: String?) {
        spotifyAccessToken = accessToken
        spotifyRefreshToken = refreshToken
    }

    /**
     * Gets the current Spotify access token
     * @return The Spotify access token or null if not available
     */
    fun getAccessToken(): String? = spotifyAccessToken

    /**
     * Gets the current Spotify refresh token
     * @return The Spotify refresh token or null if not available
     */
    fun getRefreshToken(): String? = spotifyRefreshToken

    /**
     * Clears all stored tokens (call on logout)
     */
    fun clear() {
        spotifyAccessToken = null
        spotifyRefreshToken = null
    }

    /**
     * Checks if a valid Spotify token is available
     */
    fun hasToken(): Boolean = !spotifyAccessToken.isNullOrEmpty()
}