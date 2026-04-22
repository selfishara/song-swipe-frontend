package org.ilerna.song_swipe_frontend.data.repository

import android.util.Log
import io.mockk.*
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager
import org.ilerna.song_swipe_frontend.core.network.interceptors.SpotifyPerformanceInterceptor
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * Unit tests for [SpotifyPerformanceInterceptor].
 *
 * Verifies that:
 * - Every API call is logged to Firebase Analytics (not just console)
 * - Slow responses (>500ms) trigger a separate threshold event
 * - Fast responses do NOT trigger the slow event
 * - Failed requests are also measured and logged
 * - Simulated load + add operation completes under 3 seconds
 */
class SpotifyPerformanceTest {

    private lateinit var interceptor: SpotifyPerformanceInterceptor
    private lateinit var mockAnalytics: AnalyticsManager
    private lateinit var mockChain: Interceptor.Chain

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0

        mockAnalytics = mockk(relaxed = true)
        interceptor = SpotifyPerformanceInterceptor(mockAnalytics)
        mockChain = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    // ==================== Firebase Logging Tests ====================

    @Test
    fun `every API call is logged to Firebase via logApiResponseTime`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val response = mockk<Response> {
            every { code } returns 200
        }
        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns response

        // When
        interceptor.intercept(mockChain)

        // Then - verify Firebase event is fired with correct parameters
        verify {
            mockAnalytics.logApiResponseTime(
                endpoint = "/v1/me",
                durationMs = any(),
                method = "GET",
                statusCode = 200
            )
        }
    }

    @Test
    fun `POST request is logged with correct method and endpoint`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/playlists/abc123/tracks")
            .post(okhttp3.RequestBody.create(null, ByteArray(0)))
            .build()
        val response = mockk<Response> {
            every { code } returns 201
        }
        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns response

        // When
        interceptor.intercept(mockChain)

        // Then
        verify {
            mockAnalytics.logApiResponseTime(
                endpoint = "/v1/playlists/abc123/tracks",
                durationMs = any(),
                method = "POST",
                statusCode = 201
            )
        }
    }

    // ==================== Threshold Detection Tests ====================

    @Test
    fun `slow response exceeding 500ms triggers slow_api_response event`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/recommendations")
            .build()
        val response = mockk<Response> {
            every { code } returns 200
        }
        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } answers {
            Thread.sleep(600) // Simulate a slow API response
            response
        }

        // When
        interceptor.intercept(mockChain)

        // Then - both events should fire
        verify { mockAnalytics.logApiResponseTime(any(), match { it > 500 }, any(), any()) }
        verify { mockAnalytics.logSlowApiResponse("/v1/recommendations", match { it > 500 }) }
    }

    @Test
    fun `fast response under 500ms does NOT trigger slow_api_response event`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val response = mockk<Response> {
            every { code } returns 200
        }
        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns response

        // When
        interceptor.intercept(mockChain)

        // Then - logApiResponseTime should fire, but NOT logSlowApiResponse
        verify { mockAnalytics.logApiResponseTime(any(), any(), any(), any()) }
        verify(exactly = 0) { mockAnalytics.logSlowApiResponse(any(), any()) }
    }

    // ==================== Error Handling Tests ====================

    @Test
    fun `failed request still logs duration to Firebase`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } throws IOException("Connection refused")

        // When / Then
        assertFailsWith<IOException> {
            interceptor.intercept(mockChain)
        }

        // Verify duration was still logged with statusCode 0 for failed requests
        verify {
            mockAnalytics.logApiResponseTime(
                endpoint = "/v1/me",
                durationMs = any(),
                method = "GET",
                statusCode = 0
            )
        }
    }

    // ==================== Non-Functional Requirement: Load + Add < 3s ====================

    @Test
    fun `simulated load and add track completes under 3 seconds`() {
        // Given - Two sequential API calls simulating load tracks + add to playlist
        val loadRequest = Request.Builder()
            .url("https://api.spotify.com/v1/playlists/abc123/tracks")
            .build()
        val addRequest = Request.Builder()
            .url("https://api.spotify.com/v1/playlists/abc123/tracks")
            .post(okhttp3.RequestBody.create(null, ByteArray(0)))
            .build()
        val response = mockk<Response> {
            every { code } returns 200
        }

        // When - Execute both operations and measure total time
        val totalStart = System.currentTimeMillis()

        every { mockChain.request() } returns loadRequest
        every { mockChain.proceed(any()) } returns response
        interceptor.intercept(mockChain)

        every { mockChain.request() } returns addRequest
        every { mockChain.proceed(any()) } returns response
        interceptor.intercept(mockChain)

        val totalTime = System.currentTimeMillis() - totalStart

        // Then - Total operation must complete under 3000ms (NFR limit)
        assertTrue(
            totalTime < 3000,
            "Load + add operation took ${totalTime}ms (limit: 3000ms)"
        )

        // Both calls should be logged to Firebase
        verify(exactly = 2) { mockAnalytics.logApiResponseTime(any(), any(), any(), any()) }
    }

    // ==================== Verify Average < 500ms Per Request ====================

    @Test
    fun `average response time across multiple requests stays under 500ms`() {
        // Given - Multiple API calls simulating a typical session
        val endpoints = listOf("/v1/me", "/v1/playlists/abc/tracks", "/v1/tracks/xyz")
        val response = mockk<Response> {
            every { code } returns 200
        }

        // When - Execute all requests and measure total time
        val totalStart = System.currentTimeMillis()

        for (endpoint in endpoints) {
            val request = Request.Builder()
                .url("https://api.spotify.com$endpoint")
                .build()
            every { mockChain.request() } returns request
            every { mockChain.proceed(any()) } returns response
            interceptor.intercept(mockChain)
        }

        val totalTime = System.currentTimeMillis() - totalStart
        val avgTime = totalTime / endpoints.size

        // Then - Average response time per request must be < 500ms
        assertTrue(
            avgTime < 500,
            "Average response time was ${avgTime}ms (limit: 500ms)"
        )

        // All calls should be logged to Firebase
        verify(exactly = endpoints.size) { mockAnalytics.logApiResponseTime(any(), any(), any(), any()) }
    }
}