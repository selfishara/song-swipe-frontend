package org.ilerna.song_swipe_frontend.core.network.interceptors

import android.util.Log
import io.mockk.*
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.ilerna.song_swipe_frontend.core.auth.SpotifyTokenHolder
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SpotifyAuthInterceptor
 * Focuses on token injection using SpotifyTokenHolder
 */
class SpotifyAuthInterceptorTest {

    private lateinit var interceptor: SpotifyAuthInterceptor
    private lateinit var mockChain: Interceptor.Chain

    @Before
    fun setup() {
        // Mock Android Log to prevent "Method not mocked" errors
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockChain = mockk(relaxed = true)
        interceptor = SpotifyAuthInterceptor()

        // Clear any tokens before each test
        SpotifyTokenHolder.clear()
    }

    @After
    fun tearDown() {
        // Clean up tokens after each test
        SpotifyTokenHolder.clear()
        unmockkStatic(Log::class)
    }

    // ==================== Token Injection Tests ====================

    @Test
    fun `intercept should add Authorization header when token is available`() {
        // Given
        val testToken = "spotify_access_token_123"
        SpotifyTokenHolder.setTokens(testToken, null)

        val originalRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val mockResponse = mockk<Response>()

        every { mockChain.request() } returns originalRequest
        every { mockChain.proceed(any()) } returns mockResponse

        // When
        interceptor.intercept(mockChain)

        // Then
        verify {
            mockChain.proceed(match { request ->
                request.header("Authorization") == "Bearer $testToken"
            })
        }
    }

    @Test
    fun `intercept should not add Authorization header when token is null`() {
        // Given - no token set (SpotifyTokenHolder is empty)
        val originalRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val mockResponse = mockk<Response>()

        every { mockChain.request() } returns originalRequest
        every { mockChain.proceed(any()) } returns mockResponse

        // When
        interceptor.intercept(mockChain)

        // Then
        verify {
            mockChain.proceed(match { request ->
                request.header("Authorization") == null
            })
        }
    }

    @Test
    fun `intercept should not add Authorization header when token is empty string`() {
        // Given
        SpotifyTokenHolder.setTokens("", null)

        val originalRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val mockResponse = mockk<Response>()

        every { mockChain.request() } returns originalRequest
        every { mockChain.proceed(any()) } returns mockResponse

        // When
        interceptor.intercept(mockChain)

        // Then
        verify {
            mockChain.proceed(match { request ->
                request.header("Authorization") == null
            })
        }
    }

    // ==================== Token Management Tests ====================

    @Test
    fun `intercept should use updated token after clear and set`() {
        // Given - first set a token
        SpotifyTokenHolder.setTokens("old_token", null)
        SpotifyTokenHolder.clear()
        SpotifyTokenHolder.setTokens("new_token", null)

        val originalRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val mockResponse = mockk<Response>()

        every { mockChain.request() } returns originalRequest
        every { mockChain.proceed(any()) } returns mockResponse

        // When
        interceptor.intercept(mockChain)

        // Then
        verify {
            mockChain.proceed(match { request ->
                request.header("Authorization") == "Bearer new_token"
            })
        }
    }

    @Test
    fun `intercept should preserve original request URL and method`() {
        // Given
        SpotifyTokenHolder.setTokens("test_token", null)

        val originalRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val mockResponse = mockk<Response>()

        every { mockChain.request() } returns originalRequest
        every { mockChain.proceed(any()) } returns mockResponse

        // When
        interceptor.intercept(mockChain)

        // Then
        verify {
            mockChain.proceed(match { request ->
                request.url.toString() == "https://api.spotify.com/v1/me" &&
                        request.method == "GET"
            })
        }
    }
}