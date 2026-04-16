package org.ilerna.song_swipe_frontend.data.provider

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [GenrePlaylistProvider].
 *
 * Validates genre-playlist mapping, lookup methods,
 * and edge cases for unknown genres.
 */
class GenrePlaylistProviderTest {

    private lateinit var provider: GenrePlaylistProvider

    @Before
    fun setUp() {
        provider = GenrePlaylistProvider()
    }

    // ==================== getGenres ====================

    @Test
    fun `getGenres returns all configured genres`() {
        val genres = provider.getGenres()

        assertTrue(genres.isNotEmpty())
        assertTrue(genres.contains("Pop"))
        assertTrue(genres.contains("Metal"))
        assertTrue(genres.contains("Hip Hop"))
        assertTrue(genres.contains("Electronic"))
        assertTrue(genres.contains("Reggaeton"))
    }

    // ==================== getPlaylistIdsForGenre ====================

    @Test
    fun `getPlaylistIdsForGenre returns playlist IDs for known genre`() {
        val ids = provider.getPlaylistIdsForGenre("Pop")

        assertTrue(ids.isNotEmpty())
        assertTrue(ids.all { it.isNotBlank() })
    }

    @Test
    fun `getPlaylistIdsForGenre returns empty list for unknown genre`() {
        val ids = provider.getPlaylistIdsForGenre("UnknownGenre")

        assertTrue(ids.isEmpty())
    }

    @Test
    fun `getPlaylistIdsForGenre is case-sensitive`() {
        // The provider uses exact map keys
        val ids = provider.getPlaylistIdsForGenre("pop")

        assertTrue(ids.isEmpty())
    }

    @Test
    fun `each genre has at least one playlist`() {
        for (genre in provider.getGenres()) {
            val ids = provider.getPlaylistIdsForGenre(genre)
            assertTrue(ids.isNotEmpty(), "Genre '$genre' should have at least one playlist")
        }
    }

    @Test
    fun `playlist IDs are unique within a genre`() {
        for (genre in provider.getGenres()) {
            val ids = provider.getPlaylistIdsForGenre(genre)
            assertEquals(ids.size, ids.distinct().size, "Genre '$genre' has duplicate playlist IDs")
        }
    }

    // ==================== getPrimaryPlaylistIdForGenre ====================

    @Test
    fun `getPrimaryPlaylistIdForGenre returns first playlist for known genre`() {
        val primaryId = provider.getPrimaryPlaylistIdForGenre("Pop")
        val allIds = provider.getPlaylistIdsForGenre("Pop")

        assertEquals(allIds.first(), primaryId)
    }

    @Test
    fun `getPrimaryPlaylistIdForGenre returns null for unknown genre`() {
        val primaryId = provider.getPrimaryPlaylistIdForGenre("UnknownGenre")

        assertNull(primaryId)
    }

    // ==================== DEFAULT_SET_SIZE ====================

    @Test
    fun `DEFAULT_SET_SIZE is a positive value`() {
        assertTrue(GenrePlaylistProvider.DEFAULT_SET_SIZE > 0)
    }

    @Test
    fun `DEFAULT_SET_SIZE is 50`() {
        assertEquals(50, GenrePlaylistProvider.DEFAULT_SET_SIZE)
    }
}
