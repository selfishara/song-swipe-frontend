package org.ilerna.song_swipe_frontend.core.network.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager

/**
 * Interceptor to measure API response times in the network layer.
 * Fulfills the requirement: "Measure response time... within the network layer"
 * and "Log cases where times are exceeded".
 */
class SpotifyPerformanceInterceptor(
    private val analyticsManager: AnalyticsManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Start the timer
        val startTime = System.currentTimeMillis()

        // Execute the request
        val response = chain.proceed(request)

        // Stop the timer and calculate duration
        val duration = System.currentTimeMillis() - startTime
        val method = request.method
        val endpoint = request.url.encodedPath // Extract only the path (e.g., /v1/me)

        Log.d("TEST_RENDIMIENTO", "⏱️ [$method] Request to $endpoint took: ${duration}ms")

        if (duration > 500) {
            analyticsManager.logSlowApiResponse(endpoint, duration)
        }

        return response
    }
}