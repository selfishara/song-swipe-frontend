package org.ilerna.song_swipe_frontend.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

/**
 * Unit tests for UserProfileState sealed class
 * Tests state creation and property access
 */
class UserProfileStateTest {

    // ==================== Idle State Tests ====================

    @Test
    fun `Idle should be a singleton object`() {
        // Given
        val idle1 = UserProfileState.Idle
        val idle2 = UserProfileState.Idle

        // Then
        assertEquals(idle1, idle2)
        assertIs<UserProfileState.Idle>(idle1)
    }

    @Test
    fun `Idle should be of type UserProfileState`() {
        // Given
        val state: UserProfileState = UserProfileState.Idle

        // Then
        assertIs<UserProfileState>(state)
    }

    // ==================== Loading State Tests ====================

    @Test
    fun `Loading should be a singleton object`() {
        // Given
        val loading1 = UserProfileState.Loading
        val loading2 = UserProfileState.Loading

        // Then
        assertEquals(loading1, loading2)
        assertIs<UserProfileState.Loading>(loading1)
    }

    @Test
    fun `Loading should be of type UserProfileState`() {
        // Given
        val state: UserProfileState = UserProfileState.Loading

        // Then
        assertIs<UserProfileState>(state)
    }

    // ==================== Success State Tests ====================

    @Test
    fun `Success should contain user data`() {
        // Given
        val user = User(
            id = "123",
            email = "test@example.com",
            displayName = "Test User",
            profileImageUrl = "https://example.com/avatar.jpg",
            spotifyId = "spotify123"
        )

        // When
        val state = UserProfileState.Success(user)

        // Then
        assertEquals(user, state.user)
        assertEquals("123", state.user.id)
        assertEquals("Test User", state.user.displayName)
    }

    @Test
    fun `Success states with same user should be equal`() {
        // Given
        val user = User(id = "123", email = "test@test.com", displayName = "Test")
        val state1 = UserProfileState.Success(user)
        val state2 = UserProfileState.Success(user)

        // Then
        assertEquals(state1, state2)
    }

    @Test
    fun `Success states with different users should not be equal`() {
        // Given
        val user1 = User(id = "123", email = "test@test.com", displayName = "Test1")
        val user2 = User(id = "456", email = "test@test.com", displayName = "Test2")
        val state1 = UserProfileState.Success(user1)
        val state2 = UserProfileState.Success(user2)

        // Then
        assertNotEquals(state1, state2)
    }

    // ==================== Error State Tests ====================

    @Test
    fun `Error should contain error message`() {
        // Given
        val errorMessage = "Failed to load profile"

        // When
        val state = UserProfileState.Error(errorMessage)

        // Then
        assertEquals(errorMessage, state.message)
    }

    @Test
    fun `Error states with same message should be equal`() {
        // Given
        val state1 = UserProfileState.Error("Error")
        val state2 = UserProfileState.Error("Error")

        // Then
        assertEquals(state1, state2)
    }

    @Test
    fun `Error states with different messages should not be equal`() {
        // Given
        val state1 = UserProfileState.Error("Error 1")
        val state2 = UserProfileState.Error("Error 2")

        // Then
        assertNotEquals(state1, state2)
    }

    // ==================== Type Distinction Tests ====================

    @Test
    fun `different state types should not be equal`() {
        // Given
        val idle = UserProfileState.Idle
        val loading = UserProfileState.Loading
        val success = UserProfileState.Success(User(id = "1", email = "", displayName = ""))
        val error = UserProfileState.Error("error")

        // Then
        assertNotEquals<UserProfileState>(idle, loading)
        assertNotEquals<UserProfileState>(idle, success)
        assertNotEquals<UserProfileState>(idle, error)
        assertNotEquals<UserProfileState>(loading, success)
        assertNotEquals<UserProfileState>(loading, error)
        assertNotEquals<UserProfileState>(success, error)
    }
}