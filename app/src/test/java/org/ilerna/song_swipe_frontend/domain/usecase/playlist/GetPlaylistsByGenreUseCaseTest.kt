package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyExternalUrlsDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyImageDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySimplifiedPlaylistDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.SpotifyDataSourceImpl
import org.ilerna.song_swipe_frontend.data.repository.impl.SpotifyRepositoryImpl
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class GetPlaylistsByGenreUseCaseTest {

    @Test
    fun `returns playlists mapped to domain model`() = runTest {
        // Arrange
        val spotifyDataSource = mockk<SpotifyDataSourceImpl>()

        val dto = SpotifySimplifiedPlaylistDto(
            id = "1",
            name = "Pop Hits",
            description = "Top pop songs",
            externalUrls = SpotifyExternalUrlsDto("https://spotify.com/playlist/1"),
            images = listOf(
                SpotifyImageDto(
                    url = "https://image.com",
                    height = 300,
                    width = 300
                )
            )
        )
        coEvery { spotifyDataSource.getPlaylistsByGenre("pop") } returns
                ApiResponse.create(Response.success(listOf(dto)))

        val repository = SpotifyRepositoryImpl(spotifyDataSource)
        val useCase = GetPlaylistsByGenreUseCase(repository)

        // Act
        val result = useCase("pop")

        // Assert
        assertTrue(result is NetworkResult.Success)
        val playlists = (result as NetworkResult.Success).data
        assertEquals(1, playlists.size)
        assertEquals("Pop Hits", playlists.first().name)
    }

    @Test
    fun `returns empty list when no playlists are found`() = runTest {
        val spotifyDataSource = mockk<SpotifyDataSourceImpl>()

        coEvery { spotifyDataSource.getPlaylistsByGenre("unknown") } returns
                ApiResponse.create(Response.success(emptyList()))

        val repository = SpotifyRepositoryImpl(spotifyDataSource)
        val useCase = GetPlaylistsByGenreUseCase(repository)

        val result = useCase("unknown")

        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.isEmpty())
    }

    @Test
    fun `returns error when genre is blank`() = runTest {
        val spotifyDataSource = mockk<SpotifyDataSourceImpl>()
        val repository = SpotifyRepositoryImpl(spotifyDataSource)
        val useCase = GetPlaylistsByGenreUseCase(repository)
        val result = useCase("   ")

        assertTrue(result is NetworkResult.Error)
        assertEquals("Genre cannot be empty", (result as NetworkResult.Error).message)
    }
}