package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [GetUserPlaylistsUseCase].
 *
 * Verifies that the use case correctly delegates to [SpotifyRepository.getUserPlaylists]
 * and propagates both success and error results.
 */
class GetUserPlaylistsUseCaseTest {

    private lateinit var spotifyRepository: SpotifyRepository
    private lateinit var useCase: GetUserPlaylistsUseCase

    private fun fakePlaylists(count: Int): List<Playlist> =
        (1..count).map { i ->
            Playlist(
                id = "playlist-$i",
                name = "Playlist $i",
                description = "Description $i",
                url = "https://spotify.com/playlist/playlist-$i",
                imageUrl = "https://images/cover-$i.jpg",
                isPublic = i % 2 == 0,
                externalUrl = "https://spotify.com/playlist/playlist-$i"
            )
        }

    @Before
    fun setUp() {
        spotifyRepository = mockk()
        useCase = GetUserPlaylistsUseCase(spotifyRepository)
    }

    @Test
    fun `invoke returns playlists on success`() = runTest {
        // Given
        val playlists = fakePlaylists(5)
        coEvery { spotifyRepository.getUserPlaylists() } returns NetworkResult.Success(playlists)

        // When
        val result = useCase()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(5, (result as NetworkResult.Success).data.size)
        coVerify(exactly = 1) { spotifyRepository.getUserPlaylists() }
    }

    @Test
    fun `invoke returns empty list when user has no playlists`() = runTest {
        // Given
        coEvery { spotifyRepository.getUserPlaylists() } returns NetworkResult.Success(emptyList())

        // When
        val result = useCase()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.isEmpty())
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given
        coEvery { spotifyRepository.getUserPlaylists() } returns
                NetworkResult.Error("Unauthorized", 401)

        // When
        val result = useCase()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Unauthorized", (result as NetworkResult.Error).message)
        assertEquals(401, result.code)
    }

    @Test
    fun `invoke propagates exact repository result`() = runTest {
        // Given
        val playlists = fakePlaylists(1)
        coEvery { spotifyRepository.getUserPlaylists() } returns NetworkResult.Success(playlists)

        // When
        val result = useCase()

        // Then
        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("playlist-1", data[0].id)
        assertEquals("Playlist 1", data[0].name)
    }
}
