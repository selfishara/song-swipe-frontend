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
 * @param context Application context used to initialize Firebase Analytics.
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
}