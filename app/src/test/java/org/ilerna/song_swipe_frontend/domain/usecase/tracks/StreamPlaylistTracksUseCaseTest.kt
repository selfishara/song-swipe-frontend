package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
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
 * Unit tests for [StreamPlaylistTracksUseCase].
 *
 * Validates parameter validation and Flow delegation to
 * [SpotifyRepository.streamMultiPlaylistTracks].
 */
class StreamPlaylistTracksUseCaseTest {

    private lateinit var repository: SpotifyRepository
    private lateinit var useCase: StreamPlaylistTracksUseCase

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
        useCase = StreamPlaylistTracksUseCase(repository)
    }

    // ==================== Delegation ====================

    @Test
    fun `forwards single emission from repository`() = runTest {
        val tracks = fakeTracks(3)
        every { repository.streamMultiPlaylistTracks(any(), any()) } returns
                flowOf(NetworkResult.Success(tracks))

        val emissions = useCase(listOf("pl-1")).toList()

        assertEquals(1, emissions.size)
        val first = emissions.first()
        assertTrue(first is NetworkResult.Success)
        assertEquals(3, (first as NetworkResult.Success).data.size)
    }

    @Test
    fun `forwards multiple incremental emissions in order`() = runTest {
        val batch1 = fakeTracks(2)
        val batch2 = fakeTracks(5)
        every { repository.streamMultiPlaylistTracks(any(), any()) } returns
                flowOf(
                    NetworkResult.Success(batch1),
                    NetworkResult.Success(batch2)
                )

        val emissions = useCase(listOf("pl-1", "pl-2")).toList()

        assertEquals(2, emissions.size)
        assertEquals(2, (emissions[0] as NetworkResult.Success).data.size)
        assertEquals(5, (emissions[1] as NetworkResult.Success).data.size)
    }

    @Test
    fun `forwards error emissions from repository`() = runTest {
        every { repository.streamMultiPlaylistTracks(any(), any()) } returns
                flowOf(NetworkResult.Error("Network error", 500))

        val emissions = useCase(listOf("pl-1")).toList()

        assertEquals(1, emissions.size)
        val first = emissions.first()
        assertTrue(first is NetworkResult.Error)
        assertEquals("Network error", (first as NetworkResult.Error).message)
        assertEquals(500, first.code)
    }

    @Test
    fun `delegates playlist IDs to repository verbatim`() = runTest {
        val ids = listOf("pl-a", "pl-b", "pl-c")
        every { repository.streamMultiPlaylistTracks(ids, any()) } returns
                flowOf(NetworkResult.Success(emptyList()))

        useCase(ids).toList()

        verify(exactly = 1) { repository.streamMultiPlaylistTracks(ids, any()) }
    }

    @Test
    fun `passes through custom maxTotal`() = runTest {
        every { repository.streamMultiPlaylistTracks(any(), 25) } returns
                flowOf(NetworkResult.Success(emptyList()))

        useCase(listOf("pl-1"), maxTotal = 25).toList()

        verify(exactly = 1) { repository.streamMultiPlaylistTracks(any(), 25) }
    }

    // ==================== Validation ====================

    @Test(expected = IllegalArgumentException::class)
    fun `throws when playlist IDs list is empty`() {
        useCase(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when a playlist ID is blank`() {
        useCase(listOf("pl-1", "  ", "pl-3"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when a playlist ID is empty string`() {
        useCase(listOf(""))
    }
}
