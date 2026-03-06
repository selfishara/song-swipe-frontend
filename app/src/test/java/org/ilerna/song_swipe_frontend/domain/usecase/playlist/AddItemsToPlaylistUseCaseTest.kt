package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.PlaylistRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AddItemsToPlaylistUseCase.
 *
 * These tests verify the use case behavior without requiring live API access or OAuth configuration.
 * They ensure that:
 * 1. Parameters are correctly passed to the repository
 * 2. Success responses are properly propagated
 * 3. Error responses are properly handled
 */
class AddItemsToPlaylistUseCaseTest {

    private lateinit var repository: PlaylistRepository
    private lateinit var useCase: AddItemsToPlaylistUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = AddItemsToPlaylistUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository succeeds`() = runTest {
        // Given
        val playlistId = "playlist_123"
        val trackIds = listOf("track_1", "track_2", "track_3")
        val expectedSnapshotId = "snapshot_abc123"

        coEvery {
            repository.addItemsToPlaylist(playlistId, trackIds)
        } returns NetworkResult.Success(expectedSnapshotId)

        // When
        val result = useCase.invoke(playlistId, trackIds)

        // Then
        assertTrue(result is NetworkResult.Success)
        val successResult = result as NetworkResult.Success
        assertEquals(expectedSnapshotId, successResult.data)

        coVerify(exactly = 1) {
            repository.addItemsToPlaylist(playlistId, trackIds)
        }
    }

    @Test
    fun `invoke should return Error when repository fails`() = runTest {
        // Given
        val playlistId = "playlist_123"
        val trackIds = listOf("track_1")
        val errorMessage = "Insufficient client scope"
        val errorCode = 403

        coEvery {
            repository.addItemsToPlaylist(playlistId, trackIds)
        } returns NetworkResult.Error(errorMessage, errorCode)

        // When
        val result = useCase.invoke(playlistId, trackIds)

        // Then
        assertTrue(result is NetworkResult.Error)
        val errorResult = result as NetworkResult.Error
        assertEquals(errorMessage, errorResult.message)
        assertEquals(errorCode, errorResult.code)

        coVerify(exactly = 1) {
            repository.addItemsToPlaylist(playlistId, trackIds)
        }
    }

    @Test
    fun `invoke should pass all parameters correctly to repository`() = runTest {
        // Given
        val playlistId = "playlist_456"
        val trackIds = listOf("track_a", "track_b")
        val expectedSnapshotId = "snapshot_xyz"

        coEvery {
            repository.addItemsToPlaylist(playlistId, trackIds)
        } returns NetworkResult.Success(expectedSnapshotId)

        // When
        useCase.invoke(playlistId, trackIds)

        // Then - Verify exact parameters
        coVerify(exactly = 1) {
            repository.addItemsToPlaylist(
                playlistId = playlistId,
                trackIds = trackIds
            )
        }
    }
}