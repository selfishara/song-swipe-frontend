package org.ilerna.song_swipe_frontend.presentation.screen.swipe

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
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider
import org.ilerna.song_swipe_frontend.domain.model.AlbumSimplified
import org.ilerna.song_swipe_frontend.domain.model.Artist
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.swipe.ProcessSwipeLikeUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [SwipeViewModel].
 *
 * Covers session lifecycle, active playlist picker interactions, right-swipe guard,
 * liked-track processing, and Deezer preview enrichment.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SwipeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getPlaylistTracksUseCase: GetPlaylistTracksUseCase
    private lateinit var getTrackPreviewUseCase: GetTrackPreviewUseCase
    private lateinit var processSwipeLikeUseCase: ProcessSwipeLikeUseCase
    private lateinit var getUserPlaylistsUseCase: GetUserPlaylistsUseCase
    private lateinit var getActivePlaylistUseCase: GetActivePlaylistUseCase
    private lateinit var setActivePlaylistUseCase: SetActivePlaylistUseCase
    private lateinit var swipeSessionDataStore: SwipeSessionDataStore
    private lateinit var genrePlaylistProvider: GenrePlaylistProvider

    private fun fakeTracks(count: Int = 3, withSpotifyPreview: Boolean = false): List<Track> =
        (1..count).map { i ->
            Track(
                id = "track-$i",
                name = "Song $i",
                album = AlbumSimplified(name = "Album $i", images = emptyList()),
                artists = listOf(Artist(id = "artist-$i", name = "Artist $i")),
                durationMs = 30_000,
                isPlayable = true,
                previewUrl = if (withSpotifyPreview) "https://spotify/preview-$i.mp3" else null,
                type = "track",
                uri = "spotify:track:track-$i",
                imageUrl = "https://images/cover-$i.jpg"
            )
        }

    private fun fakePlaylists(count: Int): List<Playlist> =
        (1..count).map { i ->
            Playlist(
                id = "pl-$i",
                name = "Playlist $i",
                description = null,
                url = null,
                imageUrl = null,
                isPublic = false,
                externalUrl = ""
            )
        }

    private fun createViewModel() = SwipeViewModel(
        getPlaylistTracksUseCase = getPlaylistTracksUseCase,
        getTrackPreviewUseCase = getTrackPreviewUseCase,
        processSwipeLikeUseCase = processSwipeLikeUseCase,
        getUserPlaylistsUseCase = getUserPlaylistsUseCase,
        getActivePlaylistUseCase = getActivePlaylistUseCase,
        setActivePlaylistUseCase = setActivePlaylistUseCase,
        swipeSessionDataStore = swipeSessionDataStore,
        genrePlaylistProvider = genrePlaylistProvider
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getPlaylistTracksUseCase = mockk()
        getTrackPreviewUseCase = mockk()
        processSwipeLikeUseCase = mockk(relaxed = true)
        getUserPlaylistsUseCase = mockk()
        getActivePlaylistUseCase = mockk()
        setActivePlaylistUseCase = mockk(relaxed = true)

        swipeSessionDataStore = mockk(relaxed = true)
        coEvery { swipeSessionDataStore.getGenreSync() } returns null

        genrePlaylistProvider = mockk()
        every { genrePlaylistProvider.getPlaylistIdsForGenre(any()) } returns listOf("pl-default")
        every { genrePlaylistProvider.getPlaylistIdsForGenre("Pop") } returns listOf("pl-pop-1", "pl-pop-2")
        every { genrePlaylistProvider.getPlaylistIdsForGenre("Metal") } returns listOf("pl-metal-1")

        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(emptyList())
        coEvery { getTrackPreviewUseCase(any(), any()) } returns NetworkResult.Success(null)

        // Default: no active playlist
        every { getActivePlaylistUseCase.id() } returns flowOf(null)
        every { getActivePlaylistUseCase.name() } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Session state ====================

    @Test
    fun `init without saved session leaves hasSession false`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.hasSession)
        assertTrue(viewModel.songs.isEmpty())
        assertNull(viewModel.activeGenre)
    }

    @Test
    fun `startSession sets hasSession and activeGenre`() = runTest {
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.startSession("Pop")
        advanceUntilIdle()

        assertTrue(viewModel.hasSession)
        assertEquals("Pop", viewModel.activeGenre)
    }

    @Test
    fun `startSession persists genre to DataStore`() = runTest {
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.startSession("Pop")
        advanceUntilIdle()

        coVerify { swipeSessionDataStore.saveGenre("Pop") }
    }

    @Test
    fun `restoreSession loads saved session from DataStore`() = runTest {
        val tracks = fakeTracks(5)
        coEvery { swipeSessionDataStore.getGenreSync() } returns "Metal"
        coEvery { getPlaylistTracksUseCase(listOf("pl-metal-1")) } returns NetworkResult.Success(tracks)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.hasSession)
        assertEquals("Metal", viewModel.activeGenre)
        assertEquals(0, viewModel.currentIndex)
        assertEquals(5, viewModel.songs.size)
    }

    @Test
    fun `swiping past last song clears session`() = runTest {
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(1))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        viewModel.onSwipe(SwipeDirection.LEFT)
        advanceUntilIdle()

        assertFalse(viewModel.hasSession)
        assertNull(viewModel.activeGenre)
        coVerify { swipeSessionDataStore.clearSession() }
    }

    // ==================== Active playlist flow ====================

    @Test
    fun `activePlaylistId reflects DataStore flow`() = runTest {
        every { getActivePlaylistUseCase.id() } returns flowOf("pl-active")
        every { getActivePlaylistUseCase.name() } returns flowOf("Active Playlist")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("pl-active", viewModel.activePlaylistId.value)
        assertEquals("Active Playlist", viewModel.activePlaylistName.value)
    }

    @Test
    fun `openPlaylistPicker shows picker and loads user playlists`() = runTest {
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Success(fakePlaylists(3))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.openPlaylistPicker()
        advanceUntilIdle()

        assertTrue(viewModel.showPlaylistPicker)
        assertEquals(3, viewModel.userPlaylists.size)
    }

    @Test
    fun `openPlaylistPicker leaves playlists empty when use case fails`() = runTest {
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Error("Boom", 500)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.openPlaylistPicker()
        advanceUntilIdle()

        assertTrue(viewModel.showPlaylistPicker)
        assertTrue(viewModel.userPlaylists.isEmpty())
    }

    @Test
    fun `dismissPlaylistPicker hides picker`() = runTest {
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Success(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.openPlaylistPicker()
        advanceUntilIdle()

        viewModel.dismissPlaylistPicker()

        assertFalse(viewModel.showPlaylistPicker)
    }

    @Test
    fun `changeActivePlaylist persists via use case and closes picker`() = runTest {
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Success(fakePlaylists(1))

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.openPlaylistPicker()
        advanceUntilIdle()

        val playlist = fakePlaylists(1).first()
        viewModel.changeActivePlaylist(playlist)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            setActivePlaylistUseCase(playlistId = playlist.id, playlistName = playlist.name)
        }
        assertFalse(viewModel.showPlaylistPicker)
    }

    // ==================== onSwipe ====================

    @Test
    fun `swipe LEFT advances without saving and without calling addTrack`() = runTest {
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        viewModel.onSwipe(SwipeDirection.LEFT)
        advanceUntilIdle()

        assertEquals(1, viewModel.currentIndex)
        assertTrue(viewModel.likedSongs.isEmpty())
        coVerify(exactly = 0) { processSwipeLikeUseCase.handle(any(), any()) }
    }

    @Test
    fun `swipe RIGHT with active playlist adds track and advances`() = runTest {
        every { getActivePlaylistUseCase.id() } returns flowOf("pl-active")
        every { getActivePlaylistUseCase.name() } returns flowOf("Active")
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        coEvery { processSwipeLikeUseCase.handle(any(), any()) } returns NetworkResult.Success("snap")

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        viewModel.onSwipe(SwipeDirection.RIGHT)
        advanceUntilIdle()

        assertEquals(1, viewModel.currentIndex)
        assertEquals(1, viewModel.likedSongs.size)
        assertEquals("track-1", viewModel.likedSongs[0].id)
        coVerify(exactly = 1) {
            processSwipeLikeUseCase.handle(playlistId = "pl-active", trackId = "track-1")
        }
    }

    @Test
    fun `swipe RIGHT without active playlist is blocked and opens picker`() = runTest {
        // Given: no active playlist (default)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        coEvery { getUserPlaylistsUseCase() } returns NetworkResult.Success(fakePlaylists(2))

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        // When
        viewModel.onSwipe(SwipeDirection.RIGHT)
        advanceUntilIdle()

        // Then: swipe did not advance, did not save, and picker is open
        assertEquals(0, viewModel.currentIndex)
        assertTrue(viewModel.likedSongs.isEmpty())
        assertTrue(viewModel.showPlaylistPicker)
        coVerify(exactly = 0) { processSwipeLikeUseCase.handle(any(), any()) }
    }

    @Test
    fun `swipe RIGHT on null song does nothing`() = runTest {
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSwipe(SwipeDirection.RIGHT)
        advanceUntilIdle()

        assertEquals(0, viewModel.currentIndex)
        assertTrue(viewModel.likedSongs.isEmpty())
        coVerify(exactly = 0) { processSwipeLikeUseCase.handle(any(), any()) }
    }

    @Test
    fun `swipe RIGHT still advances even if processSwipeLikeUseCase returns error`() = runTest {
        every { getActivePlaylistUseCase.id() } returns flowOf("pl-active")
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        coEvery { processSwipeLikeUseCase.handle(any(), any()) } returns NetworkResult.Error("fail")

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        viewModel.onSwipe(SwipeDirection.RIGHT)
        advanceUntilIdle()

        assertEquals(1, viewModel.currentIndex)
        assertEquals(1, viewModel.likedSongs.size)
    }

    // ==================== loadSongs ====================

    @Test
    fun `startSession loads and maps songs correctly`() = runTest {
        val tracks = fakeTracks(1, withSpotifyPreview = true)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(tracks)

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        val song = viewModel.songs.first()
        assertEquals("track-1", song.id)
        assertEquals("Song 1", song.title)
        assertEquals("Artist 1", song.artist)
        assertEquals("https://images/cover-1.jpg", song.imageUrl)
        assertEquals("https://spotify/preview-1.mp3", song.previewUrl)
    }

    @Test
    fun `loadSongs error leaves songs list empty`() = runTest {
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Error("err", 500)

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        assertTrue(viewModel.songs.isEmpty())
    }

    // ==================== Deezer enrichment ====================

    @Test
    fun `songs without Spotify preview are enriched via Deezer`() = runTest {
        val tracks = fakeTracks(2, withSpotifyPreview = false)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(tracks)
        coEvery { getTrackPreviewUseCase("Song 1", "Artist 1") } returns
                NetworkResult.Success("https://deezer/preview-1.mp3")
        coEvery { getTrackPreviewUseCase("Song 2", "Artist 2") } returns
                NetworkResult.Success("https://deezer/preview-2.mp3")

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        assertEquals("https://deezer/preview-1.mp3", viewModel.songs[0].previewUrl)
        assertEquals("https://deezer/preview-2.mp3", viewModel.songs[1].previewUrl)
    }

    @Test
    fun `Deezer failure leaves previewUrl null`() = runTest {
        val tracks = fakeTracks(1, withSpotifyPreview = false)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(tracks)
        coEvery { getTrackPreviewUseCase(any(), any()) } returns NetworkResult.Error("Deezer unavailable")

        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        assertNull(viewModel.songs[0].previewUrl)
    }

    // ==================== currentSongOrNull ====================

    @Test
    fun `currentSongOrNull returns first song after load`() = runTest {
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(2))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("Pop")
        advanceUntilIdle()

        assertEquals("track-1", viewModel.currentSongOrNull()?.id)
    }

    @Test
    fun `currentSongOrNull returns null when no session`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.currentSongOrNull())
    }
}
