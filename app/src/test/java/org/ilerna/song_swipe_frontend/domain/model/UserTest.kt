package org.ilerna.song_swipe_frontend.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for User data class
 * Focuses on constructor validation, optional field handling, and real-world scenarios
 */
class UserTest {

    // ==================== Constructor and Properties Tests ====================

    @Test
    fun `user should be created with all required fields`() {
        // When
        val user = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User"
        )

        // Then
        assertEquals("user123", user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.displayName)
        assertNull(user.profileImageUrl)
        assertNull(user.spotifyId)
    }

    @Test
    fun `user should be created with all fields including optional ones`() {
        // When
        val user = User(
            id = "user456",
            email = "complete@example.com",
            displayName = "Complete User",
            profileImageUrl = "https://example.com/avatar.jpg",
            spotifyId = "spotify789"
        )

        // Then
        assertEquals("user456", user.id)
        assertEquals("complete@example.com", user.email)
        assertEquals("Complete User", user.displayName)
        assertEquals("https://example.com/avatar.jpg", user.profileImageUrl)
        assertEquals("spotify789", user.spotifyId)
    }

    @Test
    fun `user should handle null optional fields explicitly`() {
        // When
        val user = User(
            id = "user789",
            email = "minimal@example.com",
            displayName = "Minimal User",
            profileImageUrl = null,
            spotifyId = null
        )

        // Then
        assertNull(user.profileImageUrl)
        assertNull(user.spotifyId)
    }

    @Test
    fun `user should allow empty strings for required fields`() {
        // When
        val user = User(
            id = "",
            email = "",
            displayName = ""
        )

        // Then
        assertEquals("", user.id)
        assertEquals("", user.email)
        assertEquals("", user.displayName)
    }

    // ==================== Real-world Scenario Tests ====================

    @Test
    fun `user from Spotify OAuth should have all fields`() {
        // Given - Simulating data from Supabase after Spotify OAuth
        val user = User(
            id = "550e8400-e29b-41d4-a716-446655440000",
            email = "spotify.user@example.com",
            displayName = "Spotify User",
            profileImageUrl = "https://i.scdn.co/image/ab67616d0000b273...",
            spotifyId = "31l77fd3v7b3rtgc3o6avocjpwi"
        )

        // Then
        assertEquals(36, user.id.length) // UUID length
        assertTrue(user.email.contains("@"))
        assertTrue(user.profileImageUrl!!.startsWith("https://"))
        assertEquals("31l77fd3v7b3rtgc3o6avocjpwi", user.spotifyId)
    }

    @Test
    fun `user from minimal Supabase session should work`() {
        // Given - Minimum viable user from session
        val user = User(
            id = "user-id",
            email = "minimal@example.com",
            displayName = "" // Empty display name from missing metadata
        )

        // Then - Should be valid
        assertEquals("user-id", user.id)
        assertEquals("minimal@example.com", user.email)
        assertEquals("", user.displayName)
    }

    @Test
    fun `collection of users should work correctly`() {
        // Given
        val users = listOf(
            User("1", "user1@example.com", "User 1"),
            User("2", "user2@example.com", "User 2"),
            User("3", "user3@example.com", "User 3")
        )

        // When
        val userById = users.associateBy { it.id }
        val userByEmail = users.associateBy { it.email }

        // Then
        assertEquals(3, userById.size)
        assertEquals(3, userByEmail.size)
        assertEquals("User 2", userById["2"]?.displayName)
        assertEquals("1", userByEmail["user1@example.com"]?.id)
    }
}
