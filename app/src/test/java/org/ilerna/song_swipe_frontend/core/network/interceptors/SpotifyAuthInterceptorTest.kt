package org.ilerna.song_swipe_frontend.core.network.interceptors

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for SpotifyAuthInterceptor
 * Focuses on token injection and Supabase session handling
 */
class SpotifyAuthInterceptorTest {

    private lateinit var interceptor: SpotifyAuthInterceptor
    private lateinit var mockSupabaseClient: SupabaseClient
    private lateinit var mockAuth: Auth
    private lateinit var mockChain: Interceptor.Chain

    @Before
    fun setup() {
        mockSupabaseClient = mockk(relaxed = true)
        mockAuth = mockk(relaxed = true)
        mockChain = mockk(relaxed = true)

        // Mock the auth extension property
        mockkStatic("io.github.jan.supabase.auth.AuthKt")
        every { mockSupabaseClient.auth } returns mockAuth

        interceptor = SpotifyAuthInterceptor(mockSupabaseClient)
    }

    @After
    fun tearDown() {
        unmockkStatic("io.github.jan.supabase.auth.AuthKt")
    }

    // ==================== Token Injection Tests ====================

    @Test
    fun `intercept should add Authorization header when token is available`() = runTest {
        // Given
        val testToken = "spotify_access_token_123"
        val mockSession = mockk<UserSession>()
        val originalRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val mockResponse = mockk<Response>()

        every { mockSession.providerToken } returns testToken
        coEvery { mockAuth.currentSessionOrNull() } returns mockSession
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
    fun `intercept should not add Authorization header when token is null`() = runTest {
        // Given
        val originalRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val mockResponse = mockk<Response>()

        coEvery { mockAuth.currentSessionOrNull() } returns null
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
    fun `intercept should not add Authorization header when session has no provider token`() = runTest {
        // Given
        val mockSession = mockk<UserSession>()
        val originalRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val mockResponse = mockk<Response>()

        every { mockSession.providerToken } returns null
        coEvery { mockAuth.currentSessionOrNull() } returns mockSession
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

    // ==================== Error Handling Tests ====================

    @Test
    fun `intercept should proceed without token when Supabase throws exception`() = runTest {
        // Given
        val originalRequest = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val mockResponse = mockk<Response>()

        coEvery { mockAuth.currentSessionOrNull() } throws Exception("Supabase error")
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
}
