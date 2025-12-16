package org.ilerna.song_swipe_frontend.core.config

import org.ilerna.song_swipe_frontend.BuildConfig

/**
 * Application configuration constants
 *
 * This configuration now uses BuildConfig to read credentials from local.properties
 * which allows for different environments (DEV and TEST) without exposing sensitive data in the code.
 *
 * To configure your environment:
 * 1. Copy local.properties.example to local.properties
 * 2. Fill in your credentials
 * 3. Set ACTIVE_ENVIRONMENT to "DEV" or "TEST" in local.properties
 */
object AppConfig {

    /**
     * Current active environment
     * Set in local.properties: ACTIVE_ENVIRONMENT=DEV or ACTIVE_ENVIRONMENT=TEST
     */
    private val activeEnvironment = BuildConfig.ACTIVE_ENVIRONMENT

    /**
     * Spotify Client ID based on active environment
     * Obtained from the Spotify Dashboard: https://developer.spotify.com/dashboard
     */
    val SPOTIFY_CLIENT_ID: String = when (activeEnvironment) {
        "TEST" -> BuildConfig.SPOTIFY_CLIENT_ID_TEST
        else -> BuildConfig.SPOTIFY_CLIENT_ID_DEV // Default to DEV
    }

    /**
     * Redirect URI for the authentication callback
     * Must be registered in the Spotify Dashboard
     */
    const val SPOTIFY_REDIRECT_URI = "songswipe://callback"

    /**
     * Request code for the authentication activity
     * Used to identify the result of the login flow
     */
    const val AUTH_REQUEST_CODE = 1337

    /**
     * Spotify scopes requested by the application
     * - user-read-email: Read the user's email
     * - user-read-private: Read private profile information
     * - streaming: Control music playback
     *
     * Reference: https://developer.spotify.com/documentation/web-api/concepts/scopes
     */
    val SPOTIFY_SCOPES = arrayOf(
        "user-read-email",
        "user-read-private",
        "streaming"
    )

    /**
     * Tag for application logs
     */
    const val LOG_TAG = "SongSwipe"
}
