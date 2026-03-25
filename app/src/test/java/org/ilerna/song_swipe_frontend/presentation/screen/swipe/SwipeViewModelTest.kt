package org.ilerna.song_swipe_frontend.presentation.screen.swipe

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
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.domain.model.AlbumSimplified
import org.ilerna.song_swipe_frontend.domain.model.Artist
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.AddItemToDefaultPlaylistUseCase
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
 * Follows the existing project convention: MockK mocks for use cases,
 * StandardTestDispatcher for coroutines, AAA pattern in every test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SwipeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getPlaylistTracksUseCase: GetPlaylistTracksUseCase
    private lateinit var getTrackPreviewUseCase: GetTrackPreviewUseCase
    private lateinit var getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase
    private lateinit var addItemToDefaultPlaylistUseCase: AddItemToDefaultPlaylistUseCase
    private lateinit var swipeSessionDataStore: SwipeSessionDataStore

    // Helpers

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

    /**
     * Creates the ViewModel after mocks are configured.
     * By default the DataStore returns no saved session (null playlistId),
     * so no songs are loaded on init. Call [startSession] explicitly in tests
     * that need songs.
     */
    private fun createViewModel(): SwipeViewModel =
        SwipeViewModel(
            getPlaylistTracksUseCase,
            getTrackPreviewUseCase,
            getOrCreateDefaultPlaylistUseCase,
            addItemToDefaultPlaylistUseCase,
            swipeSessionDataStore,
            supabaseUserId = "test-supabase-id",
            spotifyUserId = "test-spotify-id"
        )

    // Setup / Teardown

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getPlaylistTracksUseCase = mockk()
        getTrackPreviewUseCase = mockk()
        getOrCreateDefaultPlaylistUseCase = mockk(relaxed = true)
        addItemToDefaultPlaylistUseCase = mockk(relaxed = true)

        // Relaxed mock - suspend funs return null/0 by default, writes are no-ops
        swipeSessionDataStore = mockk(relaxed = true)
        coEvery { swipeSessionDataStore.getPlaylistIdSync() } returns null
        coEvery { swipeSessionDataStore.getGenreSync() } returns null
        coEvery { swipeSessionDataStore.getCurrentIndexSync() } returns 0

        // Default: empty track list and no Deezer previews
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(emptyList())
        coEvery { getTrackPreviewUseCase(any(), any()) } returns NetworkResult.Success(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Session state tests ---

    @Test
    fun `init without saved session leaves hasSession false`() = runTest {
        // Given: DataStore returns no saved session (default setUp stubs)

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.hasSession)
        assertTrue(viewModel.songs.isEmpty())
        assertNull(viewModel.activeGenre)
    }

    @Test
    fun `startSession sets hasSession and activeGenre`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.startSession("playlist-123", "Pop")
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.hasSession)
        assertEquals("Pop", viewModel.activeGenre)
    }

    @Test
    fun `startSession persists session to DataStore`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.startSession("playlist-123", "Pop")
        advanceUntilIdle()

        // Then
        coVerify { swipeSessionDataStore.saveSession("playlist-123", "Pop", 0) }
    }

    @Test
    fun `restoreSession loads saved session from DataStore`() = runTest {
        // Given
        val tracks = fakeTracks(5)
        coEvery { swipeSessionDataStore.getPlaylistIdSync() } returns "saved-playlist"
        coEvery { swipeSessionDataStore.getGenreSync() } returns "Metal"
        coEvery { swipeSessionDataStore.getCurrentIndexSync() } returns 2
        coEvery { getPlaylistTracksUseCase("saved-playlist") } returns NetworkResult.Success(tracks)

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.hasSession)
        assertEquals("Metal", viewModel.activeGenre)
        assertEquals(2, viewModel.currentIndex)
        assertEquals(5, viewModel.songs.size)
    }

    @Test
    fun `restoreSession with out-of-bounds index resets to 0`() = runTest {
        // Given
        val tracks = fakeTracks(3)
        coEvery { swipeSessionDataStore.getPlaylistIdSync() } returns "saved-playlist"
        coEvery { swipeSessionDataStore.getGenreSync() } returns "Pop"
        coEvery { swipeSessionDataStore.getCurrentIndexSync() } returns 99
        coEvery { getPlaylistTracksUseCase("saved-playlist") } returns NetworkResult.Success(tracks)

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(0, viewModel.currentIndex)
    }

    @Test
    fun `swiping past last song clears session`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(1))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // When: swipe past the only song
        viewModel.onSwipe(SwipeDirection.LEFT)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.hasSession)
        assertNull(viewModel.activeGenre)
        coVerify { swipeSessionDataStore.clearSession() }
    }

    @Test
    fun `swipe saves current index to DataStore`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // When
        viewModel.onSwipe(SwipeDirection.LEFT)
        advanceUntilIdle()

        // Then
        coVerify { swipeSessionDataStore.saveCurrentIndex(1) }
    }

    @Test
    fun `startSession replaces previous session`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl-1", "Pop")
        advanceUntilIdle()

        // When
        viewModel.startSession("pl-2", "Metal")
        advanceUntilIdle()

        // Then
        assertEquals("Metal", viewModel.activeGenre)
    }

    // --- loadSongs tests (triggered via startSession) ---

    @Test
    fun `startSession loads songs correctly`() = runTest {
        // Given
        val tracks = fakeTracks(3)
        coEvery { getPlaylistTracksUseCase("pl-pop") } returns NetworkResult.Success(tracks)
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.startSession("pl-pop", "Pop")
        advanceUntilIdle()

        // Then
        assertEquals(3, viewModel.songs.size)
        assertEquals("Song 1", viewModel.songs[0].title)
        assertEquals("Artist 1", viewModel.songs[0].artist)
        assertEquals(0, viewModel.currentIndex)
    }

    @Test
    fun `loadSongs maps track fields to SongUiModel correctly`() = runTest {
        // Given
        val tracks = fakeTracks(1, withSpotifyPreview = true)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(tracks)
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // Then
        val song = viewModel.songs.first()
        assertEquals("track-1", song.id)
        assertEquals("Song 1", song.title)
        assertEquals("Artist 1", song.artist)
        assertEquals("https://images/cover-1.jpg", song.imageUrl)
        assertEquals("https://spotify/preview-1.mp3", song.previewUrl)
    }

    @Test
    fun `loadSongs error keeps songs list empty`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Error("Network error", 500)
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.songs.isEmpty())
    }

    @Test
    fun `loadSongs resets currentIndex to zero`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(5))
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // Then
        assertEquals(0, viewModel.currentIndex)
    }

    // --- Deezer enrichment tests ---

    @Test
    fun `songs without Spotify preview are enriched with Deezer preview`() = runTest {
        // Given
        val tracks = fakeTracks(2, withSpotifyPreview = false)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(tracks)
        coEvery { getTrackPreviewUseCase("Song 1", "Artist 1") } returns
                NetworkResult.Success("https://deezer/preview-1.mp3")
        coEvery { getTrackPreviewUseCase("Song 2", "Artist 2") } returns
                NetworkResult.Success("https://deezer/preview-2.mp3")

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // Then
        assertEquals("https://deezer/preview-1.mp3", viewModel.songs[0].previewUrl)
        assertEquals("https://deezer/preview-2.mp3", viewModel.songs[1].previewUrl)
    }

    @Test
    fun `songs with existing Spotify preview are not overwritten by Deezer`() = runTest {
        // Given
        val tracks = fakeTracks(1, withSpotifyPreview = true)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(tracks)

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // Then
        assertEquals("https://spotify/preview-1.mp3", viewModel.songs[0].previewUrl)
    }

    @Test
    fun `Deezer enrichment failure leaves previewUrl as null`() = runTest {
        // Given
        val tracks = fakeTracks(1, withSpotifyPreview = false)
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(tracks)
        coEvery { getTrackPreviewUseCase(any(), any()) } returns NetworkResult.Error("Deezer unavailable")

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // Then
        assertNull(viewModel.songs[0].previewUrl)
    }

    // --- currentSongOrNull tests ---

    @Test
    fun `currentSongOrNull returns first song after load`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(2))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // When / Then
        assertEquals("track-1", viewModel.currentSongOrNull()?.id)
    }

    @Test
    fun `currentSongOrNull returns null when no session`() = runTest {
        // Given: no saved session (default setUp stubs)

        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.currentSongOrNull())
    }

    @Test
    fun `currentSongOrNull returns null when index exceeds list`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(1))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // When: swipe past the only song
        viewModel.onSwipe(SwipeDirection.LEFT)

        // Then
        assertNull(viewModel.currentSongOrNull())
    }

    // --- onSwipe tests ---

    @Test
    fun `swipe LEFT advances to next song without saving`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // When
        viewModel.onSwipe(SwipeDirection.LEFT)

        // Then
        assertEquals(1, viewModel.currentIndex)
        assertTrue(viewModel.likedSongs.isEmpty())
    }

    @Test
    fun `swipe RIGHT saves song and advances`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // When
        viewModel.onSwipe(SwipeDirection.RIGHT)

        // Then
        assertEquals(1, viewModel.currentIndex)
        assertEquals(1, viewModel.likedSongs.size)
        assertEquals("track-1", viewModel.likedSongs[0].id)
    }

    @Test
    fun `swipe RIGHT two different songs accumulates likedSongs`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(3))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // When
        viewModel.onSwipe(SwipeDirection.RIGHT) // saves track-1
        viewModel.onSwipe(SwipeDirection.RIGHT) // saves track-2

        // Then
        assertEquals(2, viewModel.likedSongs.size)
        assertEquals("track-1", viewModel.likedSongs[0].id)
        assertEquals("track-2", viewModel.likedSongs[1].id)
    }

    @Test
    fun `swipe on null song does nothing`() = runTest {
        // Given: empty playlist, no session started
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(emptyList())
        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onSwipe(SwipeDirection.RIGHT)

        // Then
        assertEquals(0, viewModel.currentIndex)
        assertTrue(viewModel.likedSongs.isEmpty())
    }

    @Test
    fun `multiple swipes advance index sequentially`() = runTest {
        // Given
        coEvery { getPlaylistTracksUseCase(any()) } returns NetworkResult.Success(fakeTracks(5))
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startSession("pl", "Pop")
        advanceUntilIdle()

        // When
        viewModel.onSwipe(SwipeDirection.LEFT)
        viewModel.onSwipe(SwipeDirection.RIGHT)
        viewModel.onSwipe(SwipeDirection.LEFT)

        // Then
        assertEquals(3, viewModel.currentIndex)
        assertEquals(1, viewModel.likedSongs.size) // only the RIGHT swipe
        assertEquals("track-2", viewModel.likedSongs[0].id)
    }
}
