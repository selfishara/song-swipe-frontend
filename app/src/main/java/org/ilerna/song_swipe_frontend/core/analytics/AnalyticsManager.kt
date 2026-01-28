package org.ilerna.song_swipe_frontend.core.analytics


import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics


class AnalyticsManager(context: Context) {

    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun logSpotifyLoginStart() {
        analytics.logEvent(AnalyticsEvents.SPOTIFY_LOGIN_START, null)
    }

    fun logSpotifyLoginSuccess() {
        analytics.logEvent(AnalyticsEvents.SPOTIFY_LOGIN_SUCCESS, null)
    }

    fun logSpotifyLoginError(error: Throwable) {
        val bundle = Bundle().apply {
            putString(AnalyticsEvents.ERROR_MESSAGE, error.message)
        }

        analytics.logEvent(AnalyticsEvents.SPOTIFY_LOGIN_ERROR, bundle)
        crashlytics.recordException(error)
    }
}
