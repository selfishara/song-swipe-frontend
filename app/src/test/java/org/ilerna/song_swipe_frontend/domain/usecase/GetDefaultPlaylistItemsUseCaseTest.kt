package org.ilerna.song_swipe_frontend.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.AlbumSimplified
import org.ilerna.song_swipe_frontend.domain.model.Artist
import org.ilerna.song_swipe_frontend.domain.model.Image
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.repository.AuthRepository
import org.ilerna.song_swipe_frontend.domain.repository.DefaultPlaylistRepository
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetDefaultPlaylistItemsUseCase
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class GetDefaultPlaylistItemsUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var defaultPlaylistRepository: DefaultPlaylistRepository
    private lateinit var spotifyRepository: SpotifyRepository

    private lateinit var useCase: GetDefaultPlaylistItemsUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        defaultPlaylistRepository = mockk()
        spotifyRepository = mockk()

        useCase = GetDefaultPlaylistItemsUseCase(
            authRepository,
            defaultPlaylistRepository,
            spotifyRepository
        )
    }

    private fun fakeUser() = User(
        id = "user-id",
        email = "test@test.com",
        displayName = "Test User"
    )

    private fun fakeImage() = Image(
        url = "https://test.com/image.jpg",
        height = 0,
        width = 0
    )

    private fun fakeAlbum() = AlbumSimplified(
        name = "Test Album",
        images = listOf(fakeImage())
    )

    private fun fakeArtist() = Artist(
        id = "artist-id",
        name = "Test Artist"
    )

    private fun fakeTrack(id: String) = Track(
        id = id,
        name = "Track $id",
        album = fakeAlbum(),
        artists = listOf(fakeArtist()),
        durationMs = 200000,
        isPlayable = true,
        previewUrl = null,
        type = "track",
        uri = "spotify:track:$id",
        imageUrl = null
    )

    @Test
    fun `fails when no users are authenticated`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        val result = useCase()

        assertTrue(result is NetworkResult.Error)
        assertEquals("User not authenticated", (result as NetworkResult.Error).message)

        coVerify(exactly = 0) { defaultPlaylistRepository.getDefaultPlaylist(any()) }
        coVerify(exactly = 0) { spotifyRepository.getPlaylistTracksDto(any()) }
    }

    @Test
    fun `returns empty list when default playlist does not exist`() = runTest {
        val user = fakeUser()

        coEvery { authRepository.getCurrentUser() } returns user
        coEvery {
            defaultPlaylistRepository.getDefaultPlaylist(user.id)
        } returns NetworkResult.Success(null)

        val result = useCase()

        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.isEmpty())

        coVerify(exactly = 1) {
            defaultPlaylistRepository.getDefaultPlaylist(user.id)
        }
        coVerify(exactly = 0) {
            spotifyRepository.getPlaylistTracksDto(any())
        }
    }

    @Test
    fun `returns error when default playlist repository fails`() = runTest {
        val user = fakeUser()

        coEvery { authRepository.getCurrentUser() } returns user
        coEvery {
            defaultPlaylistRepository.getDefaultPlaylist(user.id)
        } returns NetworkResult.Error("Supabase error", 500)

        val result = useCase()

        assertTrue(result is NetworkResult.Error)
        assertEquals("Supabase error", (result as NetworkResult.Error).message)
        assertEquals(500, (result as NetworkResult.Error).code)

        coVerify(exactly = 0) {
            spotifyRepository.getPlaylistTracksDto(any())
        }
    }

    @Test
    fun `returns tracks when default playlist exists`() = runTest {
        val user = fakeUser()

        coEvery { authRepository.getCurrentUser() } returns user

        val playlist = Playlist(
            id = "playlist-id",
            name = "Default Playlist",
            description = null,
            url = null,
            imageUrl = null,
            isPublic = false,
            externalUrl = ""
        )

        coEvery {
            defaultPlaylistRepository.getDefaultPlaylist(user.id)
        } returns NetworkResult.Success(playlist)

        val tracks = listOf(
            fakeTrack("1"),
            fakeTrack("2")
        )

        coEvery {
            spotifyRepository.getPlaylistTracksDto(playlist.id)
        } returns NetworkResult.Success(tracks)

        val result = useCase()

        assertTrue(result is NetworkResult.Success)
        assertEquals(2, (result as NetworkResult.Success).data.size)

        coVerify(exactly = 1) {
            defaultPlaylistRepository.getDefaultPlaylist(user.id)
        }
        coVerify(exactly = 1) {
            spotifyRepository.getPlaylistTracksDto(playlist.id)
        }
    }
}