package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.repository.DefaultPlaylistRepository
import org.ilerna.song_swipe_frontend.domain.repository.PlaylistRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetOrCreateDefaultPlaylistUseCase.
 *
 * Covers the main flows:
 * 1. Playlist already exists in Supabase → returns it, no Spotify call
 * 2. No playlist in Supabase → creates on Spotify → persists → returns it
 * 3. Error checking Supabase → propagates error
 * 4. Error creating on Spotify → propagates error
 * 5. Spotify create succeeds but Supabase save fails → still returns playlist
 */
class GetOrCreateDefaultPlaylistUseCaseTest {

    private lateinit var defaultPlaylistRepository: DefaultPlaylistRepository
    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var useCase: GetOrCreateDefaultPlaylistUseCase

    private val supabaseUserId = "supabase-uuid-123"
    private val spotifyUserId = "spotify_user_id"

    private val existingPlaylist = Playlist(
        id = "playlist_abc",
        name = "SongSwipe Likes",
        description = "Songs you liked on SongSwipe",
        url = "https://open.spotify.com/playlist/playlist_abc",
        imageUrl = null,
        isPublic = false,
        externalUrl = "https://open.spotify.com/playlist/playlist_abc"
    )

    @Before
    fun setup() {
        defaultPlaylistRepository = mockk()
        playlistRepository = mockk()
        useCase = GetOrCreateDefaultPlaylistUseCase(defaultPlaylistRepository, playlistRepository)
    }

    @Test
    fun `returns existing playlist without calling Spotify when already in Supabase`() = runTest {
        // Given
        coEvery {
            defaultPlaylistRepository.getDefaultPlaylist(supabaseUserId)
        } returns NetworkResult.Success(existingPlaylist)

        // When
        val result = useCase(supabaseUserId, spotifyUserId)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(existingPlaylist.id, (result as NetworkResult.Success).data.id)

        coVerify(exactly = 0) { playlistRepository.createPlaylist(any(), any(), any(), any()) }
        coVerify(exactly = 0) { defaultPlaylistRepository.saveDefaultPlaylist(any(), any(), any(), any()) }
    }

    @Test
    fun `creates playlist on Spotify and persists in Supabase when none exists`() = runTest {
        // Given - no playlist in Supabase
        coEvery {
            defaultPlaylistRepository.getDefaultPlaylist(supabaseUserId)
        } returns NetworkResult.Success(null)

        val createdPlaylist = existingPlaylist.copy(id = "new_playlist_xyz")
        coEvery {
            playlistRepository.createPlaylist(
                userId = spotifyUserId,
                name = "SongSwipe Likes",
                description = "Songs you liked on SongSwipe",
                isPublic = false
            )
        } returns NetworkResult.Success(createdPlaylist)

        coEvery {
            defaultPlaylistRepository.saveDefaultPlaylist(
                userId = supabaseUserId,
                spotifyPlaylistId = createdPlaylist.id,
                playlistName = createdPlaylist.name,
                playlistUrl = createdPlaylist.externalUrl
            )
        } returns NetworkResult.Success(Unit)

        // When
        val result = useCase(supabaseUserId, spotifyUserId)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(createdPlaylist.id, (result as NetworkResult.Success).data.id)

        coVerify(exactly = 1) {
            playlistRepository.createPlaylist(spotifyUserId, "SongSwipe Likes", "Songs you liked on SongSwipe", false)
        }
        coVerify(exactly = 1) {
            defaultPlaylistRepository.saveDefaultPlaylist(supabaseUserId, createdPlaylist.id, createdPlaylist.name, createdPlaylist.externalUrl)
        }
    }

    @Test
    fun `propagates error when Supabase check fails`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery {
            defaultPlaylistRepository.getDefaultPlaylist(supabaseUserId)
        } returns NetworkResult.Error(errorMessage)

        // When
        val result = useCase(supabaseUserId, spotifyUserId)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(errorMessage, (result as NetworkResult.Error).message)

        coVerify(exactly = 0) { playlistRepository.createPlaylist(any(), any(), any(), any()) }
    }

    @Test
    fun `propagates error when Spotify playlist creation fails`() = runTest {
        // Given
        coEvery {
            defaultPlaylistRepository.getDefaultPlaylist(supabaseUserId)
        } returns NetworkResult.Success(null)

        val errorMessage = "403 Insufficient client scope"
        coEvery {
            playlistRepository.createPlaylist(any(), any(), any(), any())
        } returns NetworkResult.Error(errorMessage)

        // When
        val result = useCase(supabaseUserId, spotifyUserId)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(errorMessage, (result as NetworkResult.Error).message)

        coVerify(exactly = 0) { defaultPlaylistRepository.saveDefaultPlaylist(any(), any(), any(), any()) }
    }

    @Test
    fun `returns playlist even when Supabase persistence fails after Spotify creation`() = runTest {
        // Given
        coEvery {
            defaultPlaylistRepository.getDefaultPlaylist(supabaseUserId)
        } returns NetworkResult.Success(null)

        val createdPlaylist = existingPlaylist.copy(id = "new_playlist_xyz")
        coEvery {
            playlistRepository.createPlaylist(any(), any(), any(), any())
        } returns NetworkResult.Success(createdPlaylist)

        coEvery {
            defaultPlaylistRepository.saveDefaultPlaylist(any(), any(), any(), any())
        } returns NetworkResult.Error("Supabase insert failed")

        // When
        val result = useCase(supabaseUserId, spotifyUserId)

        // Then - playlist was created on Spotify so we still return it
        assertTrue(result is NetworkResult.Success)
        assertEquals(createdPlaylist.id, (result as NetworkResult.Success).data.id)
    }
}
