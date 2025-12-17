package org.ilerna.song_swipe_frontend.core.auth

import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for SpotifyTokenHolder
 * Tests token storage, retrieval, and clearing functionality
 */
class SpotifyTokenHolderTest {

    @Before
    fun setup() {
        // Ensure clean state before each test
        SpotifyTokenHolder.clear()
    }

    @After
    fun tearDown() {
        // Clean up after each test
        SpotifyTokenHolder.clear()
    }

    // ==================== Token Storage Tests ====================

    @Test
    fun `setTokens should store access token correctly`() {
        // Given
        val accessToken = "test_access_token_123"
        val refreshToken = "test_refresh_token_456"

        // When
        SpotifyTokenHolder.setTokens(accessToken, refreshToken)

        // Then
        assertEquals(accessToken, SpotifyTokenHolder.getAccessToken())
        assertEquals(refreshToken, SpotifyTokenHolder.getRefreshToken())
    }

    @Test
    fun `setTokens should handle null refresh token`() {
        // Given
        val accessToken = "test_access_token"

        // When
        SpotifyTokenHolder.setTokens(accessToken, null)

        // Then
        assertEquals(accessToken, SpotifyTokenHolder.getAccessToken())
        assertNull(SpotifyTokenHolder.getRefreshToken())
    }

    @Test
    fun `setTokens should overwrite existing tokens`() {
        // Given
        SpotifyTokenHolder.setTokens("old_access", "old_refresh")

        // When
        SpotifyTokenHolder.setTokens("new_access", "new_refresh")

        // Then
        assertEquals("new_access", SpotifyTokenHolder.getAccessToken())
        assertEquals("new_refresh", SpotifyTokenHolder.getRefreshToken())
    }

    // ==================== Token Retrieval Tests ====================

    @Test
    fun `getAccessToken should return null when no token set`() {
        // Given - no tokens set (clean state from setup)

        // When
        val result = SpotifyTokenHolder.getAccessToken()

        // Then
        assertNull(result)
    }

    @Test
    fun `getRefreshToken should return null when no token set`() {
        // Given - no tokens set

        // When
        val result = SpotifyTokenHolder.getRefreshToken()

        // Then
        assertNull(result)
    }

    // ==================== Clear Tests ====================

    @Test
    fun `clear should remove all stored tokens`() {
        // Given
        SpotifyTokenHolder.setTokens("access", "refresh")

        // When
        SpotifyTokenHolder.clear()

        // Then
        assertNull(SpotifyTokenHolder.getAccessToken())
        assertNull(SpotifyTokenHolder.getRefreshToken())
    }

    // ==================== hasToken Tests ====================

    @Test
    fun `hasToken should return true when access token is set`() {
        // Given
        SpotifyTokenHolder.setTokens("valid_token", null)

        // When
        val result = SpotifyTokenHolder.hasToken()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasToken should return false when no token is set`() {
        // Given - no tokens set

        // When
        val result = SpotifyTokenHolder.hasToken()

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasToken should return false when token is empty string`() {
        // Given
        SpotifyTokenHolder.setTokens("", null)

        // When
        val result = SpotifyTokenHolder.hasToken()

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasToken should return false after clear`() {
        // Given
        SpotifyTokenHolder.setTokens("token", "refresh")
        SpotifyTokenHolder.clear()

        // When
        val result = SpotifyTokenHolder.hasToken()

        // Then
        assertFalse(result)
    }
}