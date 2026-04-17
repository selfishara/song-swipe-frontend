package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [RemoveItemFromPlaylistUseCase].
 *
 * Verifies that the use case delegates to [SpotifyRepository.removeItemsFromPlaylist]
 * with the single trackId wrapped in a list, and propagates success/error results.
 */
class RemoveItemFromPlaylistUseCaseTest {

    private lateinit var spotifyRepository: SpotifyRepository
    private lateinit var useCase: RemoveItemFromPlaylistUseCase

    @Before
    fun setUp() {
        spotifyRepository = mockk()
        useCase = RemoveItemFromPlaylistUseCase(spotifyRepository)
    }

    @Test
    fun `invoke returns success with snapshot id`() = runTest {
        // Given
        val playlistId = "playlist-abc"
        val trackId = "track-123"
        coEvery {
            spotifyRepository.removeItemsFromPlaylist(playlistId, listOf(trackId))
        } returns NetworkResult.Success("snapshot-rm")

        // When
        val result = useCase(playlistId, trackId)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("snapshot-rm", (result as NetworkResult.Success).data)
    }

    @Test
    fun `invoke wraps single trackId in a list for repository`() = runTest {
        // Given
        val playlistId = "playlist-abc"
        val trackId = "track-123"
        coEvery {
            spotifyRepository.removeItemsFromPlaylist(playlistId, listOf(trackId))
        } returns NetworkResult.Success("ok")

        // When
        useCase(playlistId, trackId)

        // Then
        coVerify(exactly = 1) {
            spotifyRepository.removeItemsFromPlaylist(
                playlistId = playlistId,
                trackIds = listOf(trackId)
            )
        }
    }

    @Test
    fun `invoke propagates repository error`() = runTest {
        // Given
        coEvery {
            spotifyRepository.removeItemsFromPlaylist(any(), any())
        } returns NetworkResult.Error("Not found", 404)

        // When
        val result = useCase("playlist-abc", "track-123")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Not found", (result as NetworkResult.Error).message)
        assertEquals(404, result.code)
    }
}
