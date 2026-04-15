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
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.RemoveItemFromDefaultPlaylistUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [PlaylistViewModel].
 *
 * Covers liked tracks loading (default playlist), track deletion flow,
 * and error handling for missing use cases or invalid parameters.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase
    private lateinit var getPlaylistTracksUseCase: GetPlaylistTracksUseCase
    private lateinit var removeItemFromDefaultPlaylistUseCase: RemoveItemFromDefaultPlaylistUseCase

    private val fakePlaylist = Playlist(
        id = "playlist-123",
        name = "SongSwipe Likes",
        description = "Your liked songs",
        url = "https://spotify.com/playlist/playlist-123",
        imageUrl = null,
        isPublic = false,
        externalUrl = "https://spotify.com/playlist/playlist-123"
    )

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
        getOrCreateDefaultPlaylistUseCase = mockk()
        getPlaylistTracksUseCase = mockk()
        removeItemFromDefaultPlaylistUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        withDefaultUseCase: Boolean = true,
        withTracksUseCase: Boolean = true,
        withRemoveUseCase: Boolean = true
    ): PlaylistViewModel {
        return PlaylistViewModel(
            getOrCreateDefaultPlaylistUseCase = if (withDefaultUseCase) getOrCreateDefaultPlaylistUseCase else null,
            getPlaylistTracksUseCase = if (withTracksUseCase) getPlaylistTracksUseCase else null,
            removeItemFromDefaultPlaylistUseCase = if (withRemoveUseCase) removeItemFromDefaultPlaylistUseCase else null
        )
    }

    // ==================== Initial State ====================

    @Test
    fun `initial state is Idle`() = runTest {
        val viewModel = createViewModel()

        assertTrue(viewModel.likedTracksState.value is UiState.Idle)
        assertNull(viewModel.trackToDelete.value)
    }

    // ==================== loadLikedTracks ====================

    @Test
    fun `loadLikedTracks returns tracks on success`() = runTest {
        // Given
        val tracks = fakeTracks(3)
        coEvery { getOrCreateDefaultPlaylistUseCase(any(), any()) } returns NetworkResult.Success(fakePlaylist)
        coEvery { getPlaylistTracksUseCase(listOf("playlist-123")) } returns NetworkResult.Success(tracks)

        val viewModel = createViewModel()

        // When
        viewModel.loadLikedTracks("supa-id", "spotify-id")
        advanceUntilIdle()

        // Then
        val state = viewModel.likedTracksState.value
        assertTrue(state is UiState.Success)
        assertEquals(3, (state as UiState.Success).data.size)
        assertEquals("Song 1", state.data[0].title)
        assertEquals("Artist 1", state.data[0].artists)
    }

    @Test
    fun `loadLikedTracks passes playlist ID as single-element list to tracks use case`() = runTest {
        // Given
        coEvery { getOrCreateDefaultPlaylistUseCase(any(), any()) } returns NetworkResult.Success(fakePlaylist)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()

        // When
        viewModel.loadLikedTracks("supa-id", "spotify-id")
        advanceUntilIdle()

        // Then — verifies the new List<String> signature is used correctly
        coVerify { getPlaylistTracksUseCase(listOf("playlist-123")) }
    }

    @Test
    fun `loadLikedTracks sets error when default playlist use case fails`() = runTest {
        // Given
        coEvery { getOrCreateDefaultPlaylistUseCase(any(), any()) } returns
                NetworkResult.Error("Supabase error", 500)

        val viewModel = createViewModel()

        // When
        viewModel.loadLikedTracks("supa-id", "spotify-id")
        advanceUntilIdle()

        // Then
        val state = viewModel.likedTracksState.value
        assertTrue(state is UiState.Error)
        assertEquals("Supabase error", (state as UiState.Error).message)
    }

    @Test
    fun `loadLikedTracks sets error when tracks use case fails`() = runTest {
        // Given
        coEvery { getOrCreateDefaultPlaylistUseCase(any(), any()) } returns NetworkResult.Success(fakePlaylist)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Error("API error", 503)

        val viewModel = createViewModel()

        // When
        viewModel.loadLikedTracks("supa-id", "spotify-id")
        advanceUntilIdle()

        // Then
        val state = viewModel.likedTracksState.value
        assertTrue(state is UiState.Error)
        assertEquals("API error", (state as UiState.Error).message)
    }

    @Test
    fun `loadLikedTracks sets error when supabase user ID is blank`() = runTest {
        val viewModel = createViewModel()

        // When
        viewModel.loadLikedTracks("  ", "spotify-id")

        // Then
        val state = viewModel.likedTracksState.value
        assertTrue(state is UiState.Error)
        assertEquals("Missing user IDs", (state as UiState.Error).message)
    }

    @Test
    fun `loadLikedTracks sets error when spotify user ID is blank`() = runTest {
        val viewModel = createViewModel()

        // When
        viewModel.loadLikedTracks("supa-id", "")

        // Then
        val state = viewModel.likedTracksState.value
        assertTrue(state is UiState.Error)
        assertEquals("Missing user IDs", (state as UiState.Error).message)
    }

    @Test
    fun `loadLikedTracks sets error when GetOrCreateDefaultPlaylistUseCase is null`() = runTest {
        val viewModel = createViewModel(withDefaultUseCase = false)

        // When
        viewModel.loadLikedTracks("supa-id", "spotify-id")

        // Then
        val state = viewModel.likedTracksState.value
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("GetOrCreateDefaultPlaylistUseCase"))
    }

    @Test
    fun `loadLikedTracks sets error when GetPlaylistTracksUseCase is null`() = runTest {
        val viewModel = createViewModel(withTracksUseCase = false)
        coEvery { getOrCreateDefaultPlaylistUseCase(any(), any()) } returns NetworkResult.Success(fakePlaylist)

        // When
        viewModel.loadLikedTracks("supa-id", "spotify-id")

        // Then
        val state = viewModel.likedTracksState.value
        assertTrue(state is UiState.Error)
        assertTrue((state as UiState.Error).message.contains("GetPlaylistTracksUseCase"))
    }

    @Test
    fun `loadLikedTracks sets error when default playlist has blank ID`() = runTest {
        // Given
        val blankIdPlaylist = fakePlaylist.copy(id = "")
        coEvery { getOrCreateDefaultPlaylistUseCase(any(), any()) } returns NetworkResult.Success(blankIdPlaylist)

        val viewModel = createViewModel()

        // When
        viewModel.loadLikedTracks("supa-id", "spotify-id")
        advanceUntilIdle()

        // Then
        val state = viewModel.likedTracksState.value
        assertTrue(state is UiState.Error)
        assertEquals("Default playlist ID is empty", (state as UiState.Error).message)
    }

    // ==================== retryLikedTracks ====================

    @Test
    fun `retryLikedTracks delegates to loadLikedTracks`() = runTest {
        // Given
        coEvery { getOrCreateDefaultPlaylistUseCase(any(), any()) } returns NetworkResult.Success(fakePlaylist)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(2))

        val viewModel = createViewModel()

        // When
        viewModel.retryLikedTracks("supa-id", "spotify-id")
        advanceUntilIdle()

        // Then
        val state = viewModel.likedTracksState.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.size)
    }

    // ==================== Track Deletion Flow ====================

    @Test
    fun `requestDeleteTrack sets trackToDelete`() = runTest {
        val viewModel = createViewModel()
        val track = PlaylistTrackUi(id = "t1", title = "Song", artists = "Artist", imageUrl = null)

        // When
        viewModel.requestDeleteTrack(track)

        // Then
        assertEquals(track, viewModel.trackToDelete.value)
    }

    @Test
    fun `cancelDeleteTrack clears trackToDelete`() = runTest {
        val viewModel = createViewModel()
        val track = PlaylistTrackUi(id = "t1", title = "Song", artists = "Artist", imageUrl = null)
        viewModel.requestDeleteTrack(track)

        // When
        viewModel.cancelDeleteTrack()

        // Then
        assertNull(viewModel.trackToDelete.value)
    }

    @Test
    fun `confirmDeleteTrack removes track from list on success`() = runTest {
        // Given
        val tracks = fakeTracks(3)
        coEvery { getOrCreateDefaultPlaylistUseCase(any(), any()) } returns NetworkResult.Success(fakePlaylist)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(tracks)
        coEvery { removeItemFromDefaultPlaylistUseCase(any(), any(), any()) } returns
                NetworkResult.Success("snapshot-1")

        val viewModel = createViewModel()
        viewModel.loadLikedTracks("supa-id", "spotify-id")
        advanceUntilIdle()

        // Get the first track from loaded state
        val loadedTracks = (viewModel.likedTracksState.value as UiState.Success).data
        viewModel.requestDeleteTrack(loadedTracks[0])

        // When
        viewModel.confirmDeleteTrack("supa-id", "spotify-id")
        advanceUntilIdle()

        // Then
        val updatedState = viewModel.likedTracksState.value as UiState.Success
        assertEquals(2, updatedState.data.size)
        assertTrue(updatedState.data.none { it.id == loadedTracks[0].id })
        assertNull(viewModel.trackToDelete.value)
    }

    @Test
    fun `confirmDeleteTrack reloads tracks on error`() = runTest {
        // Given
        val tracks = fakeTracks(2)
        coEvery { getOrCreateDefaultPlaylistUseCase(any(), any()) } returns NetworkResult.Success(fakePlaylist)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(tracks)
        coEvery { removeItemFromDefaultPlaylistUseCase(any(), any(), any()) } returns
                NetworkResult.Error("Delete failed", 500)

        val viewModel = createViewModel()
        viewModel.loadLikedTracks("supa-id", "spotify-id")
        advanceUntilIdle()

        val loadedTracks = (viewModel.likedTracksState.value as UiState.Success).data
        viewModel.requestDeleteTrack(loadedTracks[0])

        // When
        viewModel.confirmDeleteTrack("supa-id", "spotify-id")
        advanceUntilIdle()

        // Then — loadLikedTracks is called again (initial load + reload after error = 2 calls)
        coVerify(atLeast = 2) { getOrCreateDefaultPlaylistUseCase(any(), any()) }
    }

    @Test
    fun `confirmDeleteTrack does nothing when trackToDelete is null`() = runTest {
        val viewModel = createViewModel()

        // When — no track set for deletion
        viewModel.confirmDeleteTrack("supa-id", "spotify-id")
        advanceUntilIdle()

        // Then — no use case call
        coVerify(exactly = 0) { removeItemFromDefaultPlaylistUseCase(any(), any(), any()) }
    }
}
