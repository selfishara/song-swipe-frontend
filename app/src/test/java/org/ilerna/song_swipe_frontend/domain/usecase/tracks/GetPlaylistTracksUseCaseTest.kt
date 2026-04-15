package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.AlbumSimplified
import org.ilerna.song_swipe_frontend.domain.model.Artist
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [GetPlaylistTracksUseCase].
 *
 * Validates multi-playlist input handling, parameter validation,
 * and correct delegation to [SpotifyRepository.getMultiPlaylistTracks].
 */
class GetPlaylistTracksUseCaseTest {

    private lateinit var repository: SpotifyRepository
    private lateinit var useCase: GetPlaylistTracksUseCase

    private fun fakeTracks(count: Int): List<Track> =
        (1..count).map { i ->
            Track(
                id = "track-$i",
                name = "Song $i",
                album = AlbumSimplified(name = "Album $i", images = emptyList()),
                artists = listOf(Artist(id = "artist-$i", name = "Artist $i")),
                durationMs = 30_000,
                isPlayable = true,
                previewUrl = null,
                type = "track",
                uri = "spotify:track:track-$i",
                imageUrl = "https://images/cover-$i.jpg"
            )
        }

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetPlaylistTracksUseCase(repository)
    }

    // ==================== Success Cases ====================

    @Test
    fun `returns tracks from a single playlist`() = runTest {
        // Given
        val tracks = fakeTracks(3)
        coEvery { repository.getMultiPlaylistTracks(listOf("pl-1")) } returns NetworkResult.Success(tracks)

        // When
        val result = useCase(listOf("pl-1"))

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(3, (result as NetworkResult.Success).data.size)
        coVerify(exactly = 1) { repository.getMultiPlaylistTracks(listOf("pl-1")) }
    }

    @Test
    fun `returns tracks from multiple playlists`() = runTest {
        // Given
        val tracks = fakeTracks(5)
        val playlistIds = listOf("pl-1", "pl-2", "pl-3")
        coEvery { repository.getMultiPlaylistTracks(playlistIds) } returns NetworkResult.Success(tracks)

        // When
        val result = useCase(playlistIds)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(5, (result as NetworkResult.Success).data.size)
        coVerify(exactly = 1) { repository.getMultiPlaylistTracks(playlistIds) }
    }

    @Test
    fun `returns empty list when repository returns no tracks`() = runTest {
        // Given
        coEvery { repository.getMultiPlaylistTracks(any()) } returns NetworkResult.Success(emptyList())

        // When
        val result = useCase(listOf("pl-1"))

        // Then
        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.isEmpty())
    }

    // ==================== Error Cases ====================

    @Test
    fun `returns error when repository fails`() = runTest {
        // Given
        coEvery { repository.getMultiPlaylistTracks(any()) } returns
                NetworkResult.Error("Network error", 500)

        // When
        val result = useCase(listOf("pl-1"))

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Network error", (result as NetworkResult.Error).message)
        assertEquals(500, result.code)
    }

    // ==================== Validation Cases ====================

    @Test(expected = IllegalArgumentException::class)
    fun `throws when playlist IDs list is empty`() = runTest {
        useCase(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when a playlist ID is blank`() = runTest {
        useCase(listOf("pl-1", "  ", "pl-3"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when a playlist ID is empty string`() = runTest {
        useCase(listOf(""))
    }
}
