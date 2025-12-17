package org.ilerna.song_swipe_frontend.data.repository.impl

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.put
import org.ilerna.song_swipe_frontend.core.auth.SpotifyTokenHolder
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for SupabaseAuthRepository
 * Focuses on business logic: URL parsing, token extraction, user mapping, and error handling
 */
class SupabaseAuthRepositoryTest {

    private lateinit var repository: SupabaseAuthRepository
    private lateinit var mockAuth: Auth

    @Before
    fun setup() {
        // Clear SpotifyTokenHolder before each test
        SpotifyTokenHolder.clear()

        // Mock Android Log to prevent "Method not mocked" errors
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.d(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockAuth = mockk(relaxed = true)

        // Mock the auth extension property using mockkStatic
        mockkStatic("io.github.jan.supabase.auth.AuthKt")
        every { any<SupabaseClient>().auth } returns mockAuth

        // Create repository with a simple mock client
        val mockSupabaseClient = mockk<SupabaseClient>(relaxed = true)
        repository = SupabaseAuthRepository(mockSupabaseClient)
    }

    @After
    fun tearDown() {
        unmockkStatic("io.github.jan.supabase.auth.AuthKt")
        unmockkStatic(Log::class)
        SpotifyTokenHolder.clear()
    }

    // ==================== handleAuthCallback - URL Parsing Tests ====================

    @Test
    fun `handleAuthCallback should parse valid callback URL with tokens`() = runTest {
        // Given
        val url = "songswipe://callback#access_token=abc123&refresh_token=xyz789"
        val mockSession = mockk<UserSession>()
        val mockUser = mockk<UserInfo>()

        every { mockUser.id } returns "user123"
        every { mockUser.email } returns "test@example.com"
        every { mockSession.user } returns mockUser

        coEvery { mockAuth.importAuthToken(any(), any(), any(), any()) } just Runs
        coEvery { mockAuth.currentSessionOrNull() } returns mockSession

        // When
        val result = repository.handleAuthCallback(url)

        // Then
        assertTrue(result is AuthState.Success)
        assertEquals("user123", result.authorizationCode)
        coVerify {
            mockAuth.importAuthToken(
                accessToken = "abc123",
                refreshToken = "xyz789",
                retrieveUser = true,
                autoRefresh = true
            )
        }
    }

    @Test
    fun `handleAuthCallback should return error when tokens are missing`() = runTest {
        // Given - URL without required tokens
        val url = "songswipe://callback#error=access_denied"

        // When
        val result = repository.handleAuthCallback(url)

        // Then
        assertTrue(result is AuthState.Error)
        assertEquals("Invalid authentication response", result.message)
        coVerify(exactly = 0) { mockAuth.importAuthToken(any(), any(), any(), any()) }
    }

    @Test
    fun `handleAuthCallback should return error when session import fails`() = runTest {
        // Given
        val url = "songswipe://callback#access_token=abc123&refresh_token=xyz789"

        coEvery { mockAuth.importAuthToken(any(), any(), any(), any()) } just Runs
        coEvery { mockAuth.currentSessionOrNull() } returns null

        // When
        val result = repository.handleAuthCallback(url)

        // Then
        assertTrue(result is AuthState.Error)
        assertEquals("Failed to establish session", result.message)
    }

    @Test
    fun `handleAuthCallback should store provider_token in SpotifyTokenHolder`() = runTest {
        // Given
        val providerToken = "spotify_provider_token_123"
        val providerRefreshToken = "spotify_refresh_token_456"
        val url = "songswipe://callback#access_token=abc123&refresh_token=xyz789&provider_token=$providerToken&provider_refresh_token=$providerRefreshToken"

        val mockSession = mockk<UserSession>()
        val mockUser = mockk<UserInfo>()
        every { mockUser.id } returns "user123"
        every { mockSession.user } returns mockUser

        coEvery { mockAuth.importAuthToken(any(), any(), any(), any()) } just Runs
        coEvery { mockAuth.currentSessionOrNull() } returns mockSession

        // When
        repository.handleAuthCallback(url)

        // Then
        assertEquals(providerToken, SpotifyTokenHolder.getAccessToken())
        assertEquals(providerRefreshToken, SpotifyTokenHolder.getRefreshToken())
    }

    @Test
    fun `handleAuthCallback should handle missing provider_token gracefully`() = runTest {
        // Given - URL without provider_token
        val url = "songswipe://callback#access_token=abc123&refresh_token=xyz789"

        val mockSession = mockk<UserSession>()
        val mockUser = mockk<UserInfo>()
        every { mockUser.id } returns "user123"
        every { mockUser.email } returns "test@example.com"
        every { mockSession.user } returns mockUser

        coEvery { mockAuth.importAuthToken(any(), any(), any(), any()) } just Runs
        // Return session on first call (no polling needed)
        coEvery { mockAuth.currentSessionOrNull() } returns mockSession

        // When
        val result = repository.handleAuthCallback(url)

        // Then - Should still succeed, just without Spotify token stored
        assertTrue(result is AuthState.Success, "Expected AuthState.Success but got $result")
        assertNull(SpotifyTokenHolder.getAccessToken())
    }

    @Test
    fun `handleAuthCallback should handle exception during import`() = runTest {
        // Given
        val url = "songswipe://callback#access_token=abc123&refresh_token=xyz789"
        val exception = Exception("Network error")

        coEvery { mockAuth.importAuthToken(any(), any(), any(), any()) } throws exception

        // When
        val result = repository.handleAuthCallback(url)

        // Then
        assertTrue(result is AuthState.Error)
        assertEquals("Network error", result.message)
    }

    // ==================== getCurrentUser - User Mapping Tests ====================

    @Test
    fun `getCurrentUser should map Supabase user to domain User with all fields`() = runTest {
        // Given
        val mockUser = mockk<UserInfo>()
        every { mockUser.id } returns "user123"
        every { mockUser.email } returns "test@example.com"
        every { mockUser.userMetadata } returns kotlinx.serialization.json.buildJsonObject {
            put("name", "Test User")
            put("avatar_url", "https://example.com/avatar.jpg")
            put("provider_id", "spotify123")
        }

        coEvery { mockAuth.currentUserOrNull() } returns mockUser

        // When
        val result = repository.getCurrentUser()

        // Then
        assertEquals("user123", result?.id)
        assertEquals("test@example.com", result?.email)
        assertEquals("Test User", result?.displayName)
        assertEquals("https://example.com/avatar.jpg", result?.profileImageUrl)
        assertEquals("spotify123", result?.spotifyId)
    }

    @Test
    fun `getCurrentUser should handle missing optional metadata fields`() = runTest {
        // Given
        val mockUser = mockk<UserInfo>()
        every { mockUser.id } returns "user123"
        every { mockUser.email } returns "test@example.com"
        every { mockUser.userMetadata } returns kotlinx.serialization.json.buildJsonObject { }

        coEvery { mockAuth.currentUserOrNull() } returns mockUser

        // When
        val result = repository.getCurrentUser()

        // Then
        assertEquals("user123", result?.id)
        assertEquals("test@example.com", result?.email)
        assertEquals("", result?.displayName)
        assertNull(result?.profileImageUrl)
        assertNull(result?.spotifyId)
    }

    @Test
    fun `getCurrentUser should return null when no user is authenticated`() = runTest {
        // Given
        coEvery { mockAuth.currentUserOrNull() } returns null

        // When
        val result = repository.getCurrentUser()

        // Then
        assertNull(result)
    }

    // ==================== getSpotifyAccessToken Tests ====================

    @Test
    fun `getSpotifyAccessToken should return token from SpotifyTokenHolder first`() = runTest {
        // Given - Token stored in holder
        val holderToken = "holder_spotify_token"
        SpotifyTokenHolder.setTokens(holderToken, null)

        // Session also has a token (should be ignored)
        val mockSession = mockk<UserSession>()
        every { mockSession.providerToken } returns "session_spotify_token"
        coEvery { mockAuth.currentSessionOrNull() } returns mockSession

        // When
        val result = repository.getSpotifyAccessToken()

        // Then - Should return holder token, not session token
        assertEquals(holderToken, result)
    }

    @Test
    fun `getSpotifyAccessToken should fallback to session when holder is empty`() = runTest {
        // Given - No token in holder, but session has one
        val mockSession = mockk<UserSession>()
        every { mockSession.providerToken } returns "session_spotify_token"
        coEvery { mockAuth.currentSessionOrNull() } returns mockSession

        // When
        val result = repository.getSpotifyAccessToken()

        // Then
        assertEquals("session_spotify_token", result)
    }

    @Test
    fun `getSpotifyAccessToken should return null when no token available`() = runTest {
        // Given - No token in holder and no session
        coEvery { mockAuth.currentSessionOrNull() } returns null

        // When
        val result = repository.getSpotifyAccessToken()

        // Then
        assertNull(result)
    }

    // ==================== hasActiveSession Tests ====================

    @Test
    fun `hasActiveSession should return true when session exists`() = runTest {
        // Given
        val mockSession = mockk<UserSession>()
        coEvery { mockAuth.currentSessionOrNull() } returns mockSession

        // When
        val result = repository.hasActiveSession()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasActiveSession should return false when no session exists`() = runTest {
        // Given
        coEvery { mockAuth.currentSessionOrNull() } returns null

        // When
        val result = repository.hasActiveSession()

        // Then
        assertEquals(false, result)
    }
}