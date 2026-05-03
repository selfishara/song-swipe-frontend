package org.ilerna.song_swipe_frontend.core.network.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager

/**
 * Interceptor to measure API response times in the network layer.
 *
 * Every Spotify API call is logged to Firebase Analytics via [AnalyticsManager.logApiResponseTime],
 * making response times visible in the Firebase Dashboard (event: `spotify_api_response`).
 *
 * Calls exceeding [THRESHOLD_MS] are additionally logged as `slow_api_response` events,
 * providing a clear, filterable metric for threshold violations
 */
class SpotifyPerformanceInterceptor(
    private val analyticsManager: AnalyticsManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        val endpoint = request.url.encodedPath

        try {
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime

            Log.d(TAG, "⏱️ [${request.method}] $endpoint → ${response.code} in ${duration}ms")

            // Log every API call to Firebase for dashboard visibility
            analyticsManager.logApiResponseTime(
                endpoint = endpoint,
                durationMs = duration,
                method = request.method,
                statusCode = response.code
            )

            // Additionally flag slow responses as a separate event
            if (duration > THRESHOLD_MS) {
                Log.w(TAG, "⚠️ Slow response: $endpoint took ${duration}ms (threshold: ${THRESHOLD_MS}ms)")
                analyticsManager.logSlowApiResponse(endpoint, duration)
            }

            return response
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime

            Log.e(TAG, "❌ [${request.method}] $endpoint failed after ${duration}ms: ${e.message}")

            // Log failed requests too so they appear in dashboard metrics
            analyticsManager.logApiResponseTime(
                endpoint = endpoint,
                durationMs = duration,
                method = request.method,
                statusCode = 0
            )

            if (duration > THRESHOLD_MS) {
                analyticsManager.logSlowApiResponse(endpoint, duration)
            }

            throw e
        }
    }

    companion object {
        private const val TAG = "SpotifyPerformance"

        /** Maximum acceptable response time per request in milliseconds. */
        const val THRESHOLD_MS = 500L
    }
}