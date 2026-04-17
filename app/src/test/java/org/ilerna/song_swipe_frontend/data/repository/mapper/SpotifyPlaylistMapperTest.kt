package org.ilerna.song_swipe_frontend.data.repository.mapper

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyExternalUrlsDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyImageDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySimplifiedPlaylistDto
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [SpotifyPlaylistMapper.toDomain] overload that converts
 * [SpotifySimplifiedPlaylistDto] (from GET /v1/me/playlists) into domain [Playlist].
 *
 * Focuses on null-safety and image/URL fallback rules.
 */
class SpotifyPlaylistMapperTest {

    @Test
    fun `toDomain maps a complete SpotifySimplifiedPlaylistDto to Playlist`() {
        // Given
        val dto = SpotifySimplifiedPlaylistDto(
            id = "pl-123",
            name = "Chill Vibes",
            description = "Relaxing tunes",
            isPublic = true,
            externalUrls = SpotifyExternalUrlsDto(spotify = "https://spotify.com/playlist/pl-123"),
            images = listOf(
                SpotifyImageDto(url = "https://img/large.jpg", height = 640, width = 640),
                SpotifyImageDto(url = "https://img/small.jpg", height = 64, width = 64)
            )
        )

        // When
        val playlist = SpotifyPlaylistMapper.toDomain(dto)

        // Then
        assertEquals("pl-123", playlist.id)
        assertEquals("Chill Vibes", playlist.name)
        assertEquals("Relaxing tunes", playlist.description)
        assertTrue(playlist.isPublic)
        assertEquals("https://spotify.com/playlist/pl-123", playlist.externalUrl)
        assertEquals("https://spotify.com/playlist/pl-123", playlist.url)
        assertEquals("https://img/large.jpg", playlist.imageUrl)
    }

    @Test
    fun `toDomain selects first image when multiple are present`() {
        // Given - Spotify orders images largest first
        val dto = SpotifySimplifiedPlaylistDto(
            id = "pl-1",
            name = "name",
            description = null,
            isPublic = null,
            externalUrls = null,
            images = listOf(
                SpotifyImageDto(url = "https://first.jpg", height = null, width = null),
                SpotifyImageDto(url = "https://second.jpg", height = null, width = null)
            )
        )

        // When
        val playlist = SpotifyPlaylistMapper.toDomain(dto)

        // Then
        assertEquals("https://first.jpg", playlist.imageUrl)
    }

    @Test
    fun `toDomain returns null imageUrl when images is null`() {
        // Given
        val dto = SpotifySimplifiedPlaylistDto(
            id = "pl-1",
            name = "name",
            description = null,
            isPublic = null,
            externalUrls = null,
            images = null
        )

        // When
        val playlist = SpotifyPlaylistMapper.toDomain(dto)

        // Then
        assertNull(playlist.imageUrl)
    }

    @Test
    fun `toDomain returns null imageUrl when images is empty`() {
        // Given
        val dto = SpotifySimplifiedPlaylistDto(
            id = "pl-1",
            name = "name",
            description = null,
            isPublic = null,
            externalUrls = null,
            images = emptyList()
        )

        // When
        val playlist = SpotifyPlaylistMapper.toDomain(dto)

        // Then
        assertNull(playlist.imageUrl)
    }

    @Test
    fun `toDomain defaults isPublic to false when null`() {
        // Given
        val dto = SpotifySimplifiedPlaylistDto(
            id = "pl-1",
            name = "name",
            description = null,
            isPublic = null,
            externalUrls = null,
            images = null
        )

        // When
        val playlist = SpotifyPlaylistMapper.toDomain(dto)

        // Then
        assertFalse(playlist.isPublic)
    }

    @Test
    fun `toDomain uses empty string as externalUrl fallback`() {
        // Given
        val dto = SpotifySimplifiedPlaylistDto(
            id = "pl-1",
            name = "name",
            description = null,
            isPublic = null,
            externalUrls = null,
            images = null
        )

        // When
        val playlist = SpotifyPlaylistMapper.toDomain(dto)

        // Then
        assertEquals("", playlist.externalUrl)
        assertNull(playlist.url)
    }

    @Test
    fun `toDomain falls back to empty string externalUrl when spotify url is null`() {
        // Given
        val dto = SpotifySimplifiedPlaylistDto(
            id = "pl-1",
            name = "name",
            description = null,
            isPublic = null,
            externalUrls = SpotifyExternalUrlsDto(spotify = null),
            images = null
        )

        // When
        val playlist = SpotifyPlaylistMapper.toDomain(dto)

        // Then
        assertEquals("", playlist.externalUrl)
        assertNull(playlist.url)
    }

    @Test
    fun `toDomain preserves null description`() {
        // Given
        val dto = SpotifySimplifiedPlaylistDto(
            id = "pl-1",
            name = "name",
            description = null,
            isPublic = true,
            externalUrls = null,
            images = null
        )

        // When
        val playlist = SpotifyPlaylistMapper.toDomain(dto)

        // Then
        assertNull(playlist.description)
    }
}
