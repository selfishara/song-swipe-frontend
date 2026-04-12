package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoveItemFromDefaultPlaylistUseCaseTest {

    private lateinit var useCase: RemoveItemFromDefaultPlaylistUseCase
    private val mockGetOrCreateDefaultPlaylist = mockk<GetOrCreateDefaultPlaylistUseCase>()
    private val mockSpotifyRepository = mockk<SpotifyRepository>()

    private val testPlaylist = Playlist(
        id = "playlist_1",
        name = "My Likes",
        description = null,
        url = null,
        imageUrl = null,
        isPublic = false,
        externalUrl = ""
    )

    @Before
    fun setUp() {
        useCase = RemoveItemFromDefaultPlaylistUseCase(
            getOrCreateDefaultPlaylistUseCase = mockGetOrCreateDefaultPlaylist,
            spotifyRepository = mockSpotifyRepository
        )
    }

    @Test
    fun `returns Success when track is removed successfully`() = runTest {
        coEvery { mockGetOrCreateDefaultPlaylist("user1", "spotify1") } returns
                NetworkResult.Success(testPlaylist)
        coEvery { mockSpotifyRepository.removeItemsFromPlaylist("playlist_1", listOf("track_1")) } returns
                NetworkResult.Success("snapshot_123")

        val result = useCase("user1", "spotify1", "track_1")

        assertTrue(result is NetworkResult.Success)
        assertEquals("snapshot_123", (result as NetworkResult.Success).data)
    }

    @Test
    fun `returns Error when default playlist lookup fails`() = runTest {
        coEvery { mockGetOrCreateDefaultPlaylist("user1", "spotify1") } returns
                NetworkResult.Error("Playlist not found")

        val result = useCase("user1", "spotify1", "track_1")

        assertTrue(result is NetworkResult.Error)
        assertEquals("Playlist not found", (result as NetworkResult.Error).message)
    }

    @Test
    fun `returns Error when remove API call fails`() = runTest {
        coEvery { mockGetOrCreateDefaultPlaylist("user1", "spotify1") } returns
                NetworkResult.Success(testPlaylist)
        coEvery { mockSpotifyRepository.removeItemsFromPlaylist("playlist_1", listOf("track_1")) } returns
                NetworkResult.Error("Forbidden", 403)

        val result = useCase("user1", "spotify1", "track_1")

        assertTrue(result is NetworkResult.Error)
        assertEquals(403, (result as NetworkResult.Error).code)
    }
}
