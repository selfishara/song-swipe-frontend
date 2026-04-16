package org.ilerna.song_swipe_frontend.data.repository.impl

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyAlbumDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyArtistDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyPlaylistItemDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTrackDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.SpotifyDataSourceImpl
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto
import org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for SpotifyRepositoryImpl
 * Focuses on ApiResponse to NetworkResult conversion and error handling
 */
class SpotifyRepositoryImplTest {

    private lateinit var repository: SpotifyRepositoryImpl
    private lateinit var mockDataSource: SpotifyDataSourceImpl

    @Before
    fun setup() {
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockk(relaxed = true)

        mockDataSource = mockk()
        repository = SpotifyRepositoryImpl(mockDataSource)
    }

    // ==================== Success Cases ====================

    @Test
    fun `getCurrentUserProfile should return Success when API responds successfully`() = runTest {
        // Given
        val mockDto = SpotifyUserDto(
            id = "spotify123",
            displayName = "Test User",
            email = "test@example.com",
            images = null,
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )
        val apiResponse = ApiResponse.Success(mockDto)
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("spotify123", result.data.id)
        assertEquals("test@example.com", result.data.email)
        assertEquals("Test User", result.data.displayName)
        assertNull(result.data.profileImageUrl)
        assertEquals("spotify123", result.data.spotifyId)
    }

    @Test
    fun `getCurrentUserProfile should map DTO with null displayName to User with id as displayName`() = runTest {
        // Given
        val mockDto = SpotifyUserDto(
            id = "spotify456",
            displayName = null,
            email = "fallback@example.com",
            images = null,
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )
        val apiResponse = ApiResponse.Success(mockDto)
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("spotify456", result.data.displayName)
    }

    @Test
    fun `getCurrentUserProfile should map DTO with null email to User with empty email`() = runTest {
        // Given
        val mockDto = SpotifyUserDto(
            id = "spotify789",
            displayName = "No Email User",
            email = null,
            images = null,
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )
        val apiResponse = ApiResponse.Success(mockDto)
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("", result.data.email)
    }

    // ==================== Error Cases ====================

    @Test
    fun `getCurrentUserProfile should return Error when API responds with error`() = runTest {
        // Given
        val apiResponse = ApiResponse.Error(
            code = 401,
            message = "Unauthorized",
            errorBody = null
        )
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Unauthorized", result.message)
        assertEquals(401, result.code)
    }

    @Test
    fun `getCurrentUserProfile should return Error with code when API fails`() = runTest {
        // Given
        val apiResponse = ApiResponse.Error(
            code = 404,
            message = "User not found",
            errorBody = "{\"error\": \"not_found\"}"
        )
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("User not found", result.message)
        assertEquals(404, result.code)
    }

    @Test
    fun `getCurrentUserProfile should return Error with negative code for network errors`() = runTest {
        // Given
        val apiResponse = ApiResponse.Error(
            code = -1,
            message = "Connection error",
            errorBody = null
        )
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Connection error", result.message)
        assertEquals(-1, result.code)
    }

    @Test
    fun `getCurrentUserProfile should return Error when rate limited`() = runTest {
        // Given
        val apiResponse = ApiResponse.Error(
            code = 429,
            message = "Too Many Requests",
            errorBody = null
        )
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Too Many Requests", result.message)
        assertEquals(429, result.code)
    }

    // ==================== getMultiPlaylistTracks ====================

    private fun fakePlaylistItem(id: String, isLocal: Boolean = false): SpotifyPlaylistItemDto {
        return SpotifyPlaylistItemDto(
            track = SpotifyTrackDto(
                id = id,
                name = "Song $id",
                artists = listOf(SpotifyArtistDto(id = "a-$id", name = "Artist $id")),
                album = SpotifyAlbumDto(
                    albumType = "album", artists = null, availableMarkets = null,
                    href = null, id = "alb-$id", images = emptyList(), name = "Album $id",
                    releaseDate = null, releaseDatePrecision = null, totalTracks = null,
                    type = "album", uri = null
                ),
                durationMs = 30_000,
                previewUrl = null,
                isPlayable = true,
                type = "track",
                uri = "spotify:track:$id"
            ),
            isLocal = isLocal
        )
    }

    @Test
    fun `getMultiPlaylistTracks returns success with tracks from multiple playlists`() = runTest {
        // Given
        val itemsA = listOf(fakePlaylistItem("t1"), fakePlaylistItem("t2"))
        val itemsB = listOf(fakePlaylistItem("t3"))
        coEvery { mockDataSource.getAllTracksForPlaylist("pl-a") } returns ApiResponse.Success(itemsA)
        coEvery { mockDataSource.getAllTracksForPlaylist("pl-b") } returns ApiResponse.Success(itemsB)

        // When
        val result = repository.getMultiPlaylistTracks(listOf("pl-a", "pl-b"))

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(3, (result as NetworkResult.Success).data.size)
    }

    @Test
    fun `getMultiPlaylistTracks deduplicates tracks by ID`() = runTest {
        // Given — same track appears in both playlists
        val itemsA = listOf(fakePlaylistItem("t1"), fakePlaylistItem("t2"))
        val itemsB = listOf(fakePlaylistItem("t2"), fakePlaylistItem("t3"))
        coEvery { mockDataSource.getAllTracksForPlaylist("pl-a") } returns ApiResponse.Success(itemsA)
        coEvery { mockDataSource.getAllTracksForPlaylist("pl-b") } returns ApiResponse.Success(itemsB)

        // When
        val result = repository.getMultiPlaylistTracks(listOf("pl-a", "pl-b"))

        // Then
        assertTrue(result is NetworkResult.Success)
        val trackIds = (result as NetworkResult.Success).data.map { it.id }
        assertEquals(trackIds.distinct().size, trackIds.size)
        assertEquals(3, trackIds.size)
    }

    @Test
    fun `getMultiPlaylistTracks caps at DEFAULT_SET_SIZE`() = runTest {
        // Given — more tracks than DEFAULT_SET_SIZE
        val items = (1..60).map { fakePlaylistItem("t$it") }
        coEvery { mockDataSource.getAllTracksForPlaylist("pl-large") } returns ApiResponse.Success(items)

        // When
        val result = repository.getMultiPlaylistTracks(listOf("pl-large"))

        // Then
        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.size <= GenrePlaylistProvider.DEFAULT_SET_SIZE)
    }

    @Test
    fun `getMultiPlaylistTracks skips local tracks`() = runTest {
        // Given
        val items = listOf(
            fakePlaylistItem("t1", isLocal = false),
            fakePlaylistItem("t2", isLocal = true)
        )
        coEvery { mockDataSource.getAllTracksForPlaylist("pl-1") } returns ApiResponse.Success(items)

        // When
        val result = repository.getMultiPlaylistTracks(listOf("pl-1"))

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(1, (result as NetworkResult.Success).data.size)
        assertEquals("t1", result.data[0].id)
    }

    @Test
    fun `getMultiPlaylistTracks skips null track entries`() = runTest {
        // Given
        val items = listOf(
            fakePlaylistItem("t1"),
            SpotifyPlaylistItemDto(track = null, isLocal = false)
        )
        coEvery { mockDataSource.getAllTracksForPlaylist("pl-1") } returns ApiResponse.Success(items)

        // When
        val result = repository.getMultiPlaylistTracks(listOf("pl-1"))

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(1, (result as NetworkResult.Success).data.size)
    }

    @Test
    fun `getMultiPlaylistTracks skips failed playlists without aborting`() = runTest {
        // Given — one playlist succeeds, one fails
        val items = listOf(fakePlaylistItem("t1"))
        coEvery { mockDataSource.getAllTracksForPlaylist("pl-ok") } returns ApiResponse.Success(items)
        coEvery { mockDataSource.getAllTracksForPlaylist("pl-fail") } returns
                ApiResponse.Error(code = 404, message = "Not found", errorBody = null)

        // When
        val result = repository.getMultiPlaylistTracks(listOf("pl-ok", "pl-fail"))

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(1, (result as NetworkResult.Success).data.size)
    }

    @Test
    fun `getMultiPlaylistTracks returns error when playlist IDs list is empty`() = runTest {
        // When
        val result = repository.getMultiPlaylistTracks(emptyList())

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("No playlist IDs provided", (result as NetworkResult.Error).message)
    }

    @Test
    fun `getMultiPlaylistTracks returns empty success when all playlists fail`() = runTest {
        // Given
        coEvery { mockDataSource.getAllTracksForPlaylist(any()) } returns
                ApiResponse.Error(code = 500, message = "Server error", errorBody = null)

        // When
        val result = repository.getMultiPlaylistTracks(listOf("pl-1", "pl-2"))

        // Then
        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.isEmpty())
    }
}