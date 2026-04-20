package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.CreatePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [PlaylistListViewModel].
 *
 * Covers loading playlists, setting active playlist, creating playlists,
 * input validation, and error propagation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getUserPlaylistsUseCase: GetUserPlaylistsUseCase
    private lateinit var getActivePlaylistUseCase: GetActivePlaylistUseCase
    private lateinit var setActivePlaylistUseCase: SetActivePlaylistUseCase
    private lateinit var createPlaylistUseCase: CreatePlaylistUseCase

    private fun fakePlaylists(count: Int): List<Playlist> =
        (1..count).map { i ->
            Playlist(
                id = "pl-$i",
                name = "Playlist $i",
                description = "Desc $i",
                url = null,
                imageUrl = null,
                isPublic = false,
                externalUrl = "https://spotify.com/playlist/pl-$i"
            )
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getUserPlaylistsUseCase = mockk()
        getActivePlaylistUseCase = mockk()
        setActivePlaylistUseCase = mockk(relaxed = true)
        createPlaylistUseCase = mockk()

        // Default: no active playlist
        every { getActivePlaylistUseCase.id() } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(spotifyUserId: String = "spotify-user-id") = PlaylistListViewModel(
        getUserPlaylistsUseCase = getUserPlaylistsUseCase,
        getActivePlaylistUseCase = getActivePlaylistUseCase,
        setActivePlaylistUseCase = setActivePlaylistUseCase,
        createPlaylistUseCase = createPlaylistUseCase,
        spotifyUserId = spotifyUserId
    )

    // ==================== Initial state ====================

    @Test
    fun `initial state is Idle`() = runTest {
        val viewModel = createViewModel()

        assertTrue(viewModel.playlistsState.value is UiState.Idle)
        assertNull(viewModel.createError.value)
    }

    @Test
    fun `activePlaylistId reflects DataStore flow`() = runTest {
        // Given
        every { getActivePlaylistUseCase.id() } returns flowOf("pl-42")

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("pl-42", viewModel.activePlaylistId.value)
    }

    // ==================== loadPlaylists ====================

    @Test
    fun `loadPlaylists succeeds and sets Success state`() = runTest {
        // Given
        val playlists = fakePlaylists(3)
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Success(playlists)

        val viewModel = createViewModel()

        // When
        viewModel.loadPlaylists()
        advanceUntilIdle()

        // Then
        val state = viewModel.playlistsState.value
        assertTrue(state is UiState.Success)
        assertEquals(3, state.data.size)
    }

    @Test
    fun `loadPlaylists sets Error state on failure`() = runTest {
        // Given
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Error("Unauthorized", 401)

        val viewModel = createViewModel()

        // When
        viewModel.loadPlaylists()
        advanceUntilIdle()

        // Then
        val state = viewModel.playlistsState.value
        assertTrue(state is UiState.Error)
        assertEquals("Unauthorized", state.message)
    }

    @Test
    fun `loadPlaylists handles empty result`() = runTest {
        // Given
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()

        // When
        viewModel.loadPlaylists()
        advanceUntilIdle()

        // Then
        val state = viewModel.playlistsState.value
        assertTrue(state is UiState.Success)
        assertTrue(state.data.isEmpty())
    }

    // ==================== setActivePlaylist ====================

    @Test
    fun `setActivePlaylist forwards id and name to use case`() = runTest {
        // Given
        val viewModel = createViewModel()
        val playlist = fakePlaylists(1).first()

        // When
        viewModel.setActivePlaylist(playlist)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            setActivePlaylistUseCase(
                playlistId = playlist.id,
                playlistName = playlist.name
            )
        }
    }

    // ==================== createPlaylist ====================

    @Test
    fun `createPlaylist rejects blank name and sets createError`() = runTest {
        val viewModel = createViewModel()

        // When
        viewModel.createPlaylist(name = "   ", description = "desc", isPublic = false)
        advanceUntilIdle()

        // Then
        assertEquals("Playlist name is required", viewModel.createError.value)
        coVerify(exactly = 0) { createPlaylistUseCase(any(), any(), any(), any()) }
    }

    @Test
    fun `createPlaylist rejects blank spotifyUserId and sets createError`() = runTest {
        val viewModel = createViewModel(spotifyUserId = "")

        // When
        viewModel.createPlaylist(name = "Valid", description = null, isPublic = true)
        advanceUntilIdle()

        // Then
        assertEquals("Playlist name is required", viewModel.createError.value)
        coVerify(exactly = 0) { createPlaylistUseCase(any(), any(), any(), any()) }
    }

    @Test
    fun `createPlaylist success reloads playlists and clears error`() = runTest {
        // Given
        coEvery {
            createPlaylistUseCase(any(), any(), any(), any())
        } returns NetworkResult.Success(fakePlaylists(1).first())
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Success(fakePlaylists(2))

        val viewModel = createViewModel()

        // When
        viewModel.createPlaylist(name = "New", description = "desc", isPublic = true)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.createError.value)
        assertTrue(viewModel.playlistsState.value is UiState.Success)
        coVerify(exactly = 1) { getUserPlaylistsUseCase() }
    }

    @Test
    fun `createPlaylist error sets createError without reloading`() = runTest {
        // Given
        coEvery {
            createPlaylistUseCase(any(), any(), any(), any())
        } returns NetworkResult.Error("Quota exceeded", 429)

        val viewModel = createViewModel()

        // When
        viewModel.createPlaylist(name = "New", description = null, isPublic = false)
        advanceUntilIdle()

        // Then
        assertEquals("Quota exceeded", viewModel.createError.value)
        coVerify(exactly = 0) { getUserPlaylistsUseCase() }
    }

    @Test
    fun `createPlaylist trims name and maps blank description to null`() = runTest {
        // Given
        coEvery {
            createPlaylistUseCase(any(), any(), any(), any())
        } returns NetworkResult.Success(fakePlaylists(1).first())
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel(spotifyUserId = "user-1")

        // When
        viewModel.createPlaylist(name = "  Trimmed  ", description = "   ", isPublic = true)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            createPlaylistUseCase(
                userId = "user-1",
                name = "Trimmed",
                description = null,
                isPublic = true
            )
        }
    }

    @Test
    fun `createPlaylist passes non-blank description unchanged`() = runTest {
        // Given
        coEvery {
            createPlaylistUseCase(any(), any(), any(), any())
        } returns NetworkResult.Success(fakePlaylists(1).first())
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel(spotifyUserId = "user-1")

        // When
        viewModel.createPlaylist(name = "Name", description = "My description", isPublic = false)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            createPlaylistUseCase(
                userId = "user-1",
                name = "Name",
                description = "My description",
                isPublic = false
            )
        }
    }

    // ==================== clearCreateError ====================

    @Test
    fun `clearCreateError resets createError to null`() = runTest {
        // Given
        coEvery {
            createPlaylistUseCase(any(), any(), any(), any())
        } returns NetworkResult.Error("Boom")

        val viewModel = createViewModel()
        viewModel.createPlaylist(name = "name", description = null, isPublic = false)
        advanceUntilIdle()
        assertEquals("Boom", viewModel.createError.value)

        // When
        viewModel.clearCreateError()

        // Then
        assertNull(viewModel.createError.value)
    }
}
