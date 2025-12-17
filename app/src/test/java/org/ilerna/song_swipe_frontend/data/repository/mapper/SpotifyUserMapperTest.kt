package org.ilerna.song_swipe_frontend.data.repository.mapper

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyFollowersDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyImageDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for SpotifyUserMapper
 * Focuses on DTO to domain model transformation logic
 */
class SpotifyUserMapperTest {

    // ==================== Basic Mapping Tests ====================

    @Test
    fun `toDomain should map complete SpotifyUserDto to User`() {
        // Given
        val dto = SpotifyUserDto(
            id = "spotify123",
            displayName = "John Doe",
            email = "john@example.com",
            images = listOf(
                SpotifyImageDto(url = "https://example.com/large.jpg", height = 640, width = 640)
            ),
            country = "US",
            product = "premium",
            followers = SpotifyFollowersDto(href = null, total = 100),
            uri = "spotify:user:spotify123",
            href = "https://api.spotify.com/v1/users/spotify123"
        )

        // When
        val user = SpotifyUserMapper.toDomain(dto)

        // Then
        assertEquals("spotify123", user.id)
        assertEquals("john@example.com", user.email)
        assertEquals("John Doe", user.displayName)
        assertEquals("https://example.com/large.jpg", user.profileImageUrl)
        assertEquals("spotify123", user.spotifyId)
    }

    // ==================== Null Handling Tests ====================

    @Test
    fun `toDomain should handle null email with empty string`() {
        // Given
        val dto = SpotifyUserDto(
            id = "spotify456",
            displayName = "Jane Smith",
            email = null,
            images = null,
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )

        // When
        val user = SpotifyUserMapper.toDomain(dto)

        // Then
        assertEquals("", user.email)
    }

    @Test
    fun `toDomain should fallback to id when displayName is null`() {
        // Given
        val dto = SpotifyUserDto(
            id = "spotify789",
            displayName = null,
            email = "fallback@example.com",
            images = null,
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )

        // When
        val user = SpotifyUserMapper.toDomain(dto)

        // Then
        assertEquals("spotify789", user.displayName)
    }

    @Test
    fun `toDomain should return null profileImageUrl when images is null`() {
        // Given
        val dto = SpotifyUserDto(
            id = "spotify999",
            displayName = "No Avatar User",
            email = "noavatar@example.com",
            images = null,
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )

        // When
        val user = SpotifyUserMapper.toDomain(dto)

        // Then
        assertNull(user.profileImageUrl)
    }

    @Test
    fun `toDomain should return null profileImageUrl when images is empty`() {
        // Given
        val dto = SpotifyUserDto(
            id = "spotify888",
            displayName = "Empty Images User",
            email = "empty@example.com",
            images = emptyList(),
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )

        // When
        val user = SpotifyUserMapper.toDomain(dto)

        // Then
        assertNull(user.profileImageUrl)
    }

    // ==================== Image Selection Tests ====================

    @Test
    fun `toDomain should select first image from multiple images`() {
        // Given - Spotify returns images sorted by size (largest first)
        val dto = SpotifyUserDto(
            id = "spotify321",
            displayName = "Multi Image User",
            email = "multi@example.com",
            images = listOf(
                SpotifyImageDto(url = "https://example.com/large.jpg", height = 640, width = 640),
                SpotifyImageDto(url = "https://example.com/medium.jpg", height = 300, width = 300),
                SpotifyImageDto(url = "https://example.com/small.jpg", height = 64, width = 64)
            ),
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )

        // When
        val user = SpotifyUserMapper.toDomain(dto)

        // Then - Should select the first (largest) image
        assertEquals("https://example.com/large.jpg", user.profileImageUrl)
    }

    @Test
    fun `toDomain should select first image even with null dimensions`() {
        // Given
        val dto = SpotifyUserDto(
            id = "spotify654",
            displayName = "Unknown Size User",
            email = "unknown@example.com",
            images = listOf(
                SpotifyImageDto(url = "https://example.com/unknown.jpg", height = null, width = null)
            ),
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )

        // When
        val user = SpotifyUserMapper.toDomain(dto)

        // Then
        assertEquals("https://example.com/unknown.jpg", user.profileImageUrl)
    }
}