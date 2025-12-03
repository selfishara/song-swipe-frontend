package org.ilerna.song_swipe_frontend.core.config

/**
 * Application configuration constants
 * 
 * TODO: these constants should be loaded from:
 * - BuildConfig for the CLIENT_ID
 * - A secure configuration file for sensitive information
 * - Environment variables from the CI/CD system
 */
object AppConfig {
    
    /**
     * Spotify Client ID
     * Obtained from the Spotify Dashboard: https://developer.spotify.com/dashboard
     */
    const val SPOTIFY_CLIENT_ID = "6e0eabd770ec417e9e531631ac85af6a"
    
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
