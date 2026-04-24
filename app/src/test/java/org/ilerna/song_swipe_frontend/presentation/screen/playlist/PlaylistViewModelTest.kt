package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.domain.model.AlbumSimplified
import org.ilerna.song_swipe_frontend.domain.model.Artist
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.RemoveItemFromPlaylistUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for the refactored [PlaylistViewModel] that now accepts a playlistId
 * and drives [PlaylistDetailsScreen] rather than the old default playlist flow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getPlaylistTracksUseCase: GetPlaylistTracksUseCase
    private lateinit var removeItemFromPlaylistUseCase: RemoveItemFromPlaylistUseCase

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
        Dispatchers.setMain(testDispatcher)
        getPlaylistTracksUseCase = mockk()
        removeItemFromPlaylistUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = PlaylistViewModel(
        getPlaylistTracksUseCase = getPlaylistTracksUseCase,
        removeItemFromPlaylistUseCase = removeItemFromPlaylistUseCase
    )

    // ==================== Initial state ====================

    @Test
    fun `initial state is Idle and no track pending delete`() = runTest {
        val viewModel = createViewModel()

        assertTrue(viewModel.tracksState.value is UiState.Idle)
        assertNull(viewModel.trackToDelete.value)
    }

    // ==================== loadTracks ====================

    @Test
    fun `loadTracks returns Success with mapped tracks`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(listOf("pl-1")) } returns NetworkResult.Success(fakeTracks(3))

        val viewModel = createViewModel()

        // When
        viewModel.loadTracks("pl-1")
        advanceUntilIdle()

        // Then
        val state = viewModel.tracksState.value
        assertTrue(state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals(3, data.size)
        assertEquals("track-1", data[0].id)
        assertEquals("Song 1", data[0].title)
        assertEquals("Artist 1", data[0].artists)
    }

    @Test
    fun `loadTracks forwards playlistId as single-element list`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()

        // When
        viewModel.loadTracks("pl-42")
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { getPlaylistTracksUseCase(listOf("pl-42")) }
    }

    @Test
    fun `loadTracks sets Error state on failure`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Error("Boom", 500)

        val viewModel = createViewModel()

        // When
        viewModel.loadTracks("pl-1")
        advanceUntilIdle()

        // Then
        val state = viewModel.tracksState.value
        assertTrue(state is UiState.Error)
        assertEquals("Boom", (state as UiState.Error).message)
    }

    @Test
    fun `loadTracks sets Error when playlistId is blank`() = runTest {
        val viewModel = createViewModel()

        // When
        viewModel.loadTracks("   ")

        // Then
        val state = viewModel.tracksState.value
        assertTrue(state is UiState.Error)
        assertEquals("Missing playlist ID", (state as UiState.Error).message)
        coVerify(exactly = 0) { getPlaylistTracksUseCase(any()) }
    }

    // ==================== retry ====================

    @Test
    fun `retry delegates to loadTracks`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(listOf("pl-1")) } returns NetworkResult.Success(fakeTracks(1))

        val viewModel = createViewModel()

        // When
        viewModel.retry("pl-1")
        advanceUntilIdle()

        // Then
        val state = viewModel.tracksState.value
        assertTrue(state is UiState.Success)
        coVerify(exactly = 1) { getPlaylistTracksUseCase(listOf("pl-1")) }
    }

    // ==================== requestDeleteTrack / cancelDeleteTrack ====================

    @Test
    fun `requestDeleteTrack sets trackToDelete`() = runTest {
        val viewModel = createViewModel()
        val track = PlaylistTrackUi(id = "track-1", title = "t", artists = "a", imageUrl = null)

        // When
        viewModel.requestDeleteTrack(track)

        // Then
        assertEquals(track, viewModel.trackToDelete.value)
    }

    @Test
    fun `cancelDeleteTrack clears trackToDelete`() = runTest {
        val viewModel = createViewModel()
        val track = PlaylistTrackUi(id = "track-1", title = "t", artists = "a", imageUrl = null)
        viewModel.requestDeleteTrack(track)

        // When
        viewModel.cancelDeleteTrack()

        // Then
        assertNull(viewModel.trackToDelete.value)
    }

    // ==================== confirmDeleteTrack ====================

    @Test
    fun `confirmDeleteTrack removes track from Success list on success`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(listOf("pl-1")) } returns NetworkResult.Success(fakeTracks(3))
        coEvery {
            removeItemFromPlaylistUseCase(playlistId = "pl-1", trackId = "track-2")
        } returns NetworkResult.Success("snapshot-1")

        val viewModel = createViewModel()
        viewModel.loadTracks("pl-1")
        advanceUntilIdle()

        val target = (viewModel.tracksState.value as UiState.Success).data.first { it.id == "track-2" }
        viewModel.requestDeleteTrack(target)

        // When
        viewModel.confirmDeleteTrack("pl-1")
        advanceUntilIdle()

        // Then
        val state = viewModel.tracksState.value as UiState.Success
        assertEquals(2, state.data.size)
        assertTrue(state.data.none { it.id == "track-2" })
        assertNull(viewModel.trackToDelete.value)
    }

    @Test
    fun `confirmDeleteTrack is no-op when no track pending`() = runTest {
        val viewModel = createViewModel()

        // When
        viewModel.confirmDeleteTrack("pl-1")
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { removeItemFromPlaylistUseCase(any(), any()) }
    }

    @Test
    fun `confirmDeleteTrack reloads tracks on repository error`() = runTest {
        // Given: first load succeeds with 3 tracks
        coEvery { getPlaylistTracksUseCase(listOf("pl-1")) } returns NetworkResult.Success(fakeTracks(3))
        coEvery {
            removeItemFromPlaylistUseCase(playlistId = "pl-1", trackId = "track-1")
        } returns NetworkResult.Error("Network error", 500)

        val viewModel = createViewModel()
        viewModel.loadTracks("pl-1")
        advanceUntilIdle()

        val target = (viewModel.tracksState.value as UiState.Success).data.first()
        viewModel.requestDeleteTrack(target)

        // When
        viewModel.confirmDeleteTrack("pl-1")
        advanceUntilIdle()

        // Then: tracks were reloaded (expected at least 2 calls to the tracks use case)
        coVerify(atLeast = 2) { getPlaylistTracksUseCase(listOf("pl-1")) }
    }
}
