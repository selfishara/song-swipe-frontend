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

    /** Event key: emitted for every Spotify API response (tracks average response time). */
    const val SPOTIFY_API_RESPONSE = "spotify_api_response"

    /** Event key: emitted when an API response exceeds the 500ms threshold. */
    const val SLOW_API_RESPONSE = "slow_api_response"

    // --- Parameter keys ---

    /** Parameter key: contains the message associated with an error event. */
    const val ERROR_MESSAGE = "error_message"

    /** Parameter key: the API endpoint path (e.g., /v1/me). */
    const val PARAM_ENDPOINT = "endpoint"

    /** Parameter key: the request duration in milliseconds. */
    const val PARAM_DURATION_MS = "duration_ms"

    /** Parameter key: the HTTP method (GET, POST, etc.). */
    const val PARAM_HTTP_METHOD = "http_method"

    /** Parameter key: the HTTP status code of the response. */
    const val PARAM_STATUS_CODE = "status_code"
}