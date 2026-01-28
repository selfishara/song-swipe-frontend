package org.ilerna.song_swipe_frontend.core.auth

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for SpotifyTokenHolder
 * Tests token storage, retrieval, and clearing functionality with DataStore persistence
 */
class SpotifyTokenHolderTest {

    private lateinit var fakeDataStore: FakeSpotifyTokenDataStore

    @Before
    fun setup() {
        // Reset SpotifyTokenHolder to clean state
        SpotifyTokenHolder.reset()
        
        // Initialize with fake DataStore
        fakeDataStore = FakeSpotifyTokenDataStore()
        SpotifyTokenHolder.initialize(fakeDataStore)
    }

    @After
    fun tearDown() {
        // Reset to clean state after each test
        SpotifyTokenHolder.reset()
    }

    // ==================== Initialization Tests ====================

    @Test
    fun `isInitialized should return true after initialization`() {
        // Given - initialized in setup

        // Then
        assertTrue(SpotifyTokenHolder.isInitialized)
    }

    @Test
    fun `isInitialized should return false before initialization`() {
        // Given
        SpotifyTokenHolder.reset()

        // Then
        assertFalse(SpotifyTokenHolder.isInitialized)
    }

    // ==================== Token Storage Tests ====================

    @Test
    fun `setTokens should store access token correctly`() = runTest {
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
    fun `setTokens should handle null refresh token`() = runTest {
        // Given
        val accessToken = "test_access_token"

        // When
        SpotifyTokenHolder.setTokens(accessToken, null)

        // Then
        assertEquals(accessToken, SpotifyTokenHolder.getAccessToken())
        assertNull(SpotifyTokenHolder.getRefreshToken())
    }

    @Test
    fun `setTokens should overwrite existing tokens`() = runTest {
        // Given
        SpotifyTokenHolder.setTokens("old_access", "old_refresh")

        // When
        SpotifyTokenHolder.setTokens("new_access", "new_refresh")

        // Then
        assertEquals("new_access", SpotifyTokenHolder.getAccessToken())
        assertEquals("new_refresh", SpotifyTokenHolder.getRefreshToken())
    }

    @Test
    fun `setTokens should persist to DataStore`() = runTest {
        // Given
        val accessToken = "persist_access_token"
        val refreshToken = "persist_refresh_token"

        // When
        SpotifyTokenHolder.setTokens(accessToken, refreshToken)

        // Then - verify DataStore also has the tokens
        assertEquals(accessToken, fakeDataStore.getAccessTokenSync())
        assertEquals(refreshToken, fakeDataStore.getRefreshTokenSync())
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
    fun `clear should remove all stored tokens`() = runTest {
        // Given
        SpotifyTokenHolder.setTokens("access", "refresh")

        // When
        SpotifyTokenHolder.clear()

        // Then
        assertNull(SpotifyTokenHolder.getAccessToken())
        assertNull(SpotifyTokenHolder.getRefreshToken())
    }

    @Test
    fun `clear should also clear DataStore`() = runTest {
        // Given
        SpotifyTokenHolder.setTokens("access", "refresh")

        // When
        SpotifyTokenHolder.clear()

        // Then - verify DataStore is also cleared
        assertNull(fakeDataStore.getAccessTokenSync())
        assertNull(fakeDataStore.getRefreshTokenSync())
    }

    // ==================== Load from DataStore Tests ====================

    @Test
    fun `loadFromDataStore should restore tokens from DataStore`() = runTest {
        // Given - tokens in DataStore but not in cache
        fakeDataStore.setTokens("stored_access", "stored_refresh")
        SpotifyTokenHolder.clearCacheOnly()

        // When
        val result = SpotifyTokenHolder.loadFromDataStore()

        // Then
        assertTrue(result)
        assertEquals("stored_access", SpotifyTokenHolder.getAccessToken())
        assertEquals("stored_refresh", SpotifyTokenHolder.getRefreshToken())
    }

    @Test
    fun `loadFromDataStore should return false when not initialized`() = runTest {
        // Given
        SpotifyTokenHolder.reset()

        // When
        val result = SpotifyTokenHolder.loadFromDataStore()

        // Then
        assertFalse(result)
    }

    // ==================== hasToken Tests ====================

    @Test
    fun `hasToken should return true when access token is set`() = runTest {
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
    fun `hasToken should return false when token is empty string`() = runTest {
        // Given
        SpotifyTokenHolder.setTokens("", null)

        // When
        val result = SpotifyTokenHolder.hasToken()

        // Then
        assertFalse(result)
    }

    @Test
    fun `hasToken should return false after clear`() = runTest {
        // Given
        SpotifyTokenHolder.setTokens("token", "refresh")
        SpotifyTokenHolder.clear()

        // When
        val result = SpotifyTokenHolder.hasToken()

        // Then
        assertFalse(result)
    }
}