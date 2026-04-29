package org.ilerna.song_swipe_frontend.core.analytics


import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
/**
 * Manager class responsible for logging app events and errors to analytics services.
 *
 * This class centralizes all tracking for user actions and errors. Events are logged
 * to [FirebaseAnalytics], and errors are also reported to [FirebaseCrashlytics].
 *
 * @param context Application context used to initialize Firebase Analytics
 */
class AnalyticsManager(context: Context) {

    /** Firebase Analytics instance for event logging. */
    private val analytics = FirebaseAnalytics.getInstance(context)

    /** Firebase Crashlytics instance for error reporting. */
    private val crashlytics = FirebaseCrashlytics.getInstance()

    /**
     * Logs the start of a Spotify login attempt.
     *
     * This should be called when the login flow is initiated.
     */
    fun logSpotifyLoginStart() {
        // Record that a Spotify login has been initiated
        analytics.logEvent(AnalyticsEvents.SPOTIFY_LOGIN_START, null)
    }

    /**
     * Logs a successful Spotify login.
     *
     * This should be called when the login flow completes successfully.
     */
    fun logSpotifyLoginSuccess() {
        // Record that a Spotify login completed successfully
        analytics.logEvent(AnalyticsEvents.SPOTIFY_LOGIN_SUCCESS, null)
    }

    /**
     * Logs a Spotify login error and reports it to Crashlytics.
     *
     * @param error The [Throwable] representing the error that occurred during login.
     *              Its message will be included as a parameter in the analytics event.
     */
    fun logSpotifyLoginError(error: Throwable) {
        // Record a login error event and report the exception
        val bundle = Bundle().apply {
            putString(AnalyticsEvents.ERROR_MESSAGE, error.message)
        }

        analytics.logEvent(AnalyticsEvents.SPOTIFY_LOGIN_ERROR, bundle)
        crashlytics.recordException(error)
    }

    /**
     * Logs any network-related error and reports it to Firebase Analytics and Crashlytics.
     *
     * This includes HTTP errors, IOExceptions, and any unexpected exceptions occurring during network calls.
     *
     * @param error The [Throwable] representing the network error.
     * @param url Optional URL of the request that caused the error.
     */
    fun logNetworkError(error: Throwable, url: String? = null) {
        // Prepare event parameters
        val bundle = Bundle().apply {
            putString("error_message", error.message)
            putString("error_type", error::class.java.simpleName)
            putString("request_url", url)
        }

        // Log the network error event to Firebase Analytics
        analytics.logEvent("network_error", bundle)

        // Record the exception in Firebase Crashlytics for debugging purposes
        crashlytics.recordException(error)
    }

    /**
     * Logs every Spotify API response time to Firebase Analytics.
     *
     * This allows tracking average response times, filtering by endpoint,
     * and monitoring performance trends in the Firebase Dashboard.
     *
     * @param endpoint The API endpoint path (e.g., /v1/me).
     * @param durationMs How long the request took in milliseconds.
     * @param method The HTTP method (GET, POST, PUT, DELETE).
     * @param statusCode The HTTP response status code.
     */
    fun logApiResponseTime(endpoint: String, durationMs: Long, method: String, statusCode: Int) {
        val bundle = Bundle().apply {
            putString(AnalyticsEvents.PARAM_ENDPOINT, endpoint)
            putLong(AnalyticsEvents.PARAM_DURATION_MS, durationMs)
            putString(AnalyticsEvents.PARAM_HTTP_METHOD, method)
            putInt(AnalyticsEvents.PARAM_STATUS_CODE, statusCode)
        }
        analytics.logEvent(AnalyticsEvents.SPOTIFY_API_RESPONSE, bundle)
    }

    /**
     * Logs when an API response takes longer than the accepted threshold (500ms).
     *
     * This event is separate from [logApiResponseTime] so it appears as a distinct
     * event in the Firebase Dashboard, making threshold violations immediately visible.
     *
     * @param endpoint The API endpoint that was called.
     * @param durationMs How long the request took in milliseconds.
     */
    fun logSlowApiResponse(endpoint: String, durationMs: Long) {
        val bundle = Bundle().apply {
            putString(AnalyticsEvents.PARAM_ENDPOINT, endpoint)
            putLong(AnalyticsEvents.PARAM_DURATION_MS, durationMs)
        }
        analytics.logEvent(AnalyticsEvents.SLOW_API_RESPONSE, bundle)
    }

    /**
     * Logs a swipe action (like or dislike) for a song in the feed.
     *
     * @param trackId The Spotify track ID.
     * @param trackTitle The title of the track.
     * @param direction Either "like" (right swipe) or "dislike" (left swipe).
     * @param durationMs How long the swipe action took to process in milliseconds.
     */
    fun logSwipeAction(trackId: String, trackTitle: String, direction: String, durationMs: Long) {
        val bundle = Bundle().apply {
            putString(AnalyticsEvents.PARAM_TRACK_ID, trackId)
            putString(AnalyticsEvents.PARAM_TRACK_TITLE, trackTitle)
            putString(AnalyticsEvents.PARAM_SWIPE_DIRECTION, direction)
            putLong(AnalyticsEvents.PARAM_DURATION_MS, durationMs)
        }
        analytics.logEvent(AnalyticsEvents.SWIPE_ACTION, bundle)
    }

    /**
     * Logs when a song takes more than 3 seconds to load in the swipe feed.
     *
     * @param trackId The Spotify track ID.
     * @param trackTitle The title of the track.
     * @param durationMs The actual load time in milliseconds.
     */
    fun logSwipeSongSlowLoad(trackId: String, trackTitle: String, durationMs: Long) {
        val bundle = Bundle().apply {
            putString(AnalyticsEvents.PARAM_TRACK_ID, trackId)
            putString(AnalyticsEvents.PARAM_TRACK_TITLE, trackTitle)
            putLong(AnalyticsEvents.PARAM_DURATION_MS, durationMs)
        }
        analytics.logEvent(AnalyticsEvents.SWIPE_SONG_SLOW_LOAD, bundle)
    }

    /**
     * Logs the latency and result of saving a liked track to the active playlist.
     *
     * @param trackId The Spotify track ID.
     * @param durationMs Time elapsed from swipe-right to operation completion, in milliseconds.
     * @param success Whether the save succeeded.
     */
    fun logSwipeSaveLatency(trackId: String, durationMs: Long, success: Boolean) {
        val bundle = Bundle().apply {
            putString(AnalyticsEvents.PARAM_TRACK_ID, trackId)
            putLong(AnalyticsEvents.PARAM_DURATION_MS, durationMs)
            putBoolean(AnalyticsEvents.PARAM_SAVE_SUCCESS, success)
        }
        analytics.logEvent(AnalyticsEvents.SWIPE_SAVE_LATENCY, bundle)
    }
}