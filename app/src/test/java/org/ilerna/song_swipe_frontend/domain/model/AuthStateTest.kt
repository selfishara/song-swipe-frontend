package org.ilerna.song_swipe_frontend.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for AuthState sealed class
 * Focuses on pattern matching and property access for authentication flow
 */
class AuthStateTest {

    // ==================== Pattern Matching Tests ====================

    @Test
    fun `idle should match in when expression`() {
        // Given
        val state: AuthState = AuthState.Idle

        // When
        val result = when (state) {
            is AuthState.Idle -> "idle"
            is AuthState.Loading -> "loading"
            is AuthState.Success -> "success"
            is AuthState.Error -> "error"
        }

        // Then
        assertEquals("idle", result)
    }

    @Test
    fun `loading should match in when expression`() {
        // Given
        val state: AuthState = AuthState.Loading

        // When
        val result = when (state) {
            is AuthState.Idle -> "idle"
            is AuthState.Loading -> "loading"
            is AuthState.Success -> "success"
            is AuthState.Error -> "error"
        }

        // Then
        assertEquals("loading", result)
    }

    @Test
    fun `success should match in when expression and extract value`() {
        // Given
        val state: AuthState = AuthState.Success("user789")

        // When
        val result = when (state) {
            is AuthState.Idle -> "idle"
            is AuthState.Loading -> "loading"
            is AuthState.Success -> "success: ${state.authorizationCode}"
            is AuthState.Error -> "error"
        }

        // Then
        assertEquals("success: user789", result)
    }

    @Test
    fun `error should match in when expression and extract value`() {
        // Given
        val state: AuthState = AuthState.Error("OAuth denied")

        // When
        val result = when (state) {
            is AuthState.Idle -> "idle"
            is AuthState.Loading -> "loading"
            is AuthState.Success -> "success"
            is AuthState.Error -> "error: ${state.message}"
        }

        // Then
        assertEquals("error: OAuth denied", result)
    }

    // ==================== Property Access Tests ====================

    @Test
    fun `success should extract authorizationCode correctly`() {
        // Given
        val authCode = "spotify_auth_code_abc123"
        val state = AuthState.Success(authCode)

        // When / Then
        assertEquals(authCode, state.authorizationCode)
    }

    @Test
    fun `error should extract message correctly`() {
        // Given
        val errorMessage = "Authentication failed"
        val state = AuthState.Error(errorMessage)

        // When / Then
        assertEquals(errorMessage, state.message)
    }

    // ==================== Type Hierarchy Tests ====================

    @Test
    fun `all states should be instances of AuthState`() {
        // Given
        val states = listOf(
            AuthState.Idle,
            AuthState.Loading,
            AuthState.Success("test"),
            AuthState.Error("test")
        )

        // Then
        states.forEach { state ->
            assertTrue(state is AuthState)
        }
    }

    // ==================== Edge Case Tests ====================

    @Test
    fun `success with empty string should be valid`() {
        // Given
        val state = AuthState.Success("")

        // Then
        assertEquals("", state.authorizationCode)
        assertTrue(state is AuthState.Success)
    }

    @Test
    fun `error with empty string should be valid`() {
        // Given
        val state = AuthState.Error("")

        // Then
        assertEquals("", state.message)
        assertTrue(state is AuthState.Error)
    }
}