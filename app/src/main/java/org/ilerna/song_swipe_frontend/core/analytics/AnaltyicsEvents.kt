package org.ilerna.song_swipe_frontend.core.analytics
/**
 * Object that centralizes all analytics event keys used in the app.
 *
 * These constants are used to track user actions and error occurrences via
 * an analytics service (Firebase Analytics). Using constants ensures
 * consistency and reduces the risk of typos when logging events.

 */
object AnalyticsEvents {

    /** Event key: emitted when the Spotify login process starts. */
    const val SPOTIFY_LOGIN_START = "spotify_login_start"

    /** Event key: emitted when the Spotify login process completes successfully. */
    const val SPOTIFY_LOGIN_SUCCESS = "spotify_login_success"

    /** Event key: emitted when the Spotify login process fails. */
    const val SPOTIFY_LOGIN_ERROR = "spotify_login_error"

    /** Parameter key: contains the message associated with an error event. */
    const val ERROR_MESSAGE = "error_message"
}