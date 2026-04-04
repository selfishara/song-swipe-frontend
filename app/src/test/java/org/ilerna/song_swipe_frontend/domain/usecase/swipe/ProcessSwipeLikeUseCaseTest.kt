package org.ilerna.song_swipe_frontend.domain.usecase.swipe

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.AddItemToDefaultPlaylistUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessSwipeLikeUseCaseTest {

    private lateinit var addItemToDefaultPlaylistUseCase: AddItemToDefaultPlaylistUseCase
    private lateinit var useCase: ProcessSwipeLikeUseCase

    @Before
    fun setup() {
        // Mock the dependency we inject
        addItemToDefaultPlaylistUseCase = mockk()
        // Instantiate the real class we want to test
        useCase = ProcessSwipeLikeUseCase(addItemToDefaultPlaylistUseCase)
    }

    @Test
    fun `should succeed on first attempt`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returns NetworkResult.Success("ok")

        val result = useCase.handle("user1", "spotify1", "track1")

        coVerify(exactly = 1) { addItemToDefaultPlaylistUseCase(any(), any(), any()) }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `should retry once and succeed`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returnsMany listOf(
            NetworkResult.Error("fail"),
            NetworkResult.Success("ok")
        )

        val result = useCase.handle("user1", "spotify1", "track1")

        coVerify(exactly = 2) { addItemToDefaultPlaylistUseCase(any(), any(), any()) }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `should retry twice and succeed`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returnsMany listOf(
            NetworkResult.Error("fail"),
            NetworkResult.Error("fail"),
            NetworkResult.Success("ok")
        )

        val result = useCase.handle("user1", "spotify1", "track1")

        coVerify(exactly = 3) { addItemToDefaultPlaylistUseCase(any(), any(), any()) }
        assertTrue(result is NetworkResult.Success)
    }

    // Total failure: Exhausts all 3 attempts and returns Error
    @Test
    fun `should fail after 3 attempts`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returns NetworkResult.Error("fail")

        val result = useCase.handle("user1", "spotify1", "track1")

        coVerify(exactly = 3) { addItemToDefaultPlaylistUseCase(any(), any(), any()) }
        assertTrue(result is NetworkResult.Error)
    }

    // No more than 3 attempts: Verify the strict limit
    @Test
    fun `should not exceed 3 attempts`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returns NetworkResult.Error("fail")

        useCase.handle("user1", "spotify1", "track1")

        coVerify(exactly = 3) { addItemToDefaultPlaylistUseCase(any(), any(), any()) }
    }

    @Test
    fun `should complete within 3 seconds`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returns NetworkResult.Success("ok")

        val start = System.currentTimeMillis()
        useCase.handle("user1", "spotify1", "track1")
        val end = System.currentTimeMillis()

        assertTrue("El test tardó demasiado", (end - start) < 3000)
    }

    // Return last error: If it fails 3 times, the message should be from the final failure
    @Test
    fun `should return last error`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returnsMany listOf(
            NetworkResult.Error("error 1"),
            NetworkResult.Error("error 2"),
            NetworkResult.Error("final")
        )

        val result = useCase.handle("user1", "spotify1", "track1")

        assertEquals("final", (result as NetworkResult.Error).message)
    }

    @Test
    fun `should handle empty track id`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), "") } returns NetworkResult.Error("invalid")

        val result = useCase.handle("user1", "spotify1", "")

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `should always call underlying use case`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returns NetworkResult.Success("ok")

        useCase.handle("user1", "spotify1", "track1")

        coVerify(atLeast = 1) { addItemToDefaultPlaylistUseCase("user1", "spotify1", "track1") }
    }
}