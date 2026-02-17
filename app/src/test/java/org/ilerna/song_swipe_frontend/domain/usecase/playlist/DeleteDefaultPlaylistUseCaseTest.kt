package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.DefaultPlaylistRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteDefaultPlaylistUseCase.
 *
 * Covers:
 * 1. Successful deletion delegates to repository
 * 2. Repository error is propagated
 */
class DeleteDefaultPlaylistUseCaseTest {

    private lateinit var defaultPlaylistRepository: DefaultPlaylistRepository
    private lateinit var useCase: DeleteDefaultPlaylistUseCase

    private val userId = "supabase-uuid-123"

    @Before
    fun setup() {
        defaultPlaylistRepository = mockk()
        useCase = DeleteDefaultPlaylistUseCase(defaultPlaylistRepository)
    }

    @Test
    fun `returns Success when repository deletes successfully`() = runTest {
        // Given
        coEvery {
            defaultPlaylistRepository.deleteDefaultPlaylist(userId)
        } returns NetworkResult.Success(Unit)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result is NetworkResult.Success)
        coVerify(exactly = 1) { defaultPlaylistRepository.deleteDefaultPlaylist(userId) }
    }

    @Test
    fun `propagates Error when repository deletion fails`() = runTest {
        // Given
        val errorMessage = "Row not found"
        coEvery {
            defaultPlaylistRepository.deleteDefaultPlaylist(userId)
        } returns NetworkResult.Error(errorMessage)

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(errorMessage, (result as NetworkResult.Error).message)
    }
}
