package org.ilerna.song_swipe_frontend.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.repository.PlaylistRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CreatePlaylistUseCase.
 *
 * These tests verify the use case behavior without requiring live API access or OAuth configuration.
 * They ensure that:
 * 1. Parameters are correctly passed to the repository
 * 2. Success responses are properly propagated
 * 3. Error responses are properly handled
 */
class CreatePlaylistUseCaseTest {

    private lateinit var repository: PlaylistRepository
    private lateinit var useCase: CreatePlaylistUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = CreatePlaylistUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository succeeds`() = runTest {
        // Given
        val userId = "test_user_123"
        val playlistName = "My Test Playlist"
        val description = "Test description"
        val isPublic = false
        val expectedPlaylist = Playlist(
            id = "playlist_123",
            name = playlistName,
            description = description,
            url = "https://open.spotify.com/playlist/playlist_123",
            imageUrl = null,
            isPublic = isPublic,
            externalUrl = "https://open.spotify.com/playlist/playlist_123"
        )

        coEvery {
            repository.createPlaylist(userId, playlistName, description, isPublic)
        } returns NetworkResult.Success(expectedPlaylist)

        // When
        val result = useCase.invoke(userId, playlistName, description, isPublic)

        // Then
        assertTrue(result is NetworkResult.Success)
        val successResult = result as NetworkResult.Success
        assertEquals(expectedPlaylist.id, successResult.data.id)
        assertEquals(expectedPlaylist.name, successResult.data.name)
        assertEquals(expectedPlaylist.description, successResult.data.description)
        assertEquals(expectedPlaylist.isPublic, successResult.data.isPublic)

        coVerify(exactly = 1) {
            repository.createPlaylist(userId, playlistName, description, isPublic)
        }
    }

    @Test
    fun `invoke should return Error when repository fails`() = runTest {
        // Given
        val userId = "test_user_123"
        val playlistName = "My Test Playlist"
        val description = "Test description"
        val isPublic = false
        val errorMessage = "Insufficient client scope"
        val errorCode = 403

        coEvery {
            repository.createPlaylist(userId, playlistName, description, isPublic)
        } returns NetworkResult.Error(errorMessage, errorCode)

        // When
        val result = useCase.invoke(userId, playlistName, description, isPublic)

        // Then
        assertTrue(result is NetworkResult.Error)
        val errorResult = result as NetworkResult.Error
        assertEquals(errorMessage, errorResult.message)
        assertEquals(errorCode, errorResult.code)

        coVerify(exactly = 1) {
            repository.createPlaylist(userId, playlistName, description, isPublic)
        }
    }

    @Test
    fun `invoke should pass all parameters correctly to repository`() = runTest {
        // Given
        val userId = "user_456"
        val playlistName = "Another Playlist"
        val description = null // Testing with null description
        val isPublic = true
        val expectedPlaylist = Playlist(
            id = "playlist_456",
            name = playlistName,
            description = description,
            url = "https://open.spotify.com/playlist/playlist_456",
            imageUrl = null,
            isPublic = isPublic,
            externalUrl = "https://open.spotify.com/playlist/playlist_456"
        )

        coEvery {
            repository.createPlaylist(userId, playlistName, description, isPublic)
        } returns NetworkResult.Success(expectedPlaylist)

        // When
        useCase.invoke(userId, playlistName, description, isPublic)

        // Then - Verify exact parameters
        coVerify(exactly = 1) {
            repository.createPlaylist(
                userId = userId,
                name = playlistName,
                description = description,
                isPublic = isPublic
            )
        }
    }
}
