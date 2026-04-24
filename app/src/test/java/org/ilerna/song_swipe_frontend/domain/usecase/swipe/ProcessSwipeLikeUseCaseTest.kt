package org.ilerna.song_swipe_frontend.domain.usecase.swipe

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.AddItemToPlaylistUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessSwipeLikeUseCaseTest {

    private lateinit var addItemToPlaylistUseCase: AddItemToPlaylistUseCase
    private lateinit var useCase: ProcessSwipeLikeUseCase

    @Before
    fun setup() {
        addItemToPlaylistUseCase = mockk()
        useCase = ProcessSwipeLikeUseCase(addItemToPlaylistUseCase)
    }

    @Test
    fun `should succeed on first attempt`() = runTest {
        coEvery { addItemToPlaylistUseCase(any(), any()) } returns NetworkResult.Success("ok")

        val result = useCase.handle("playlist1", "track1")

        coVerify(exactly = 1) { addItemToPlaylistUseCase(any(), any()) }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `should retry once and succeed`() = runTest {
        coEvery { addItemToPlaylistUseCase(any(), any()) } returnsMany listOf(
            NetworkResult.Error("fail"),
            NetworkResult.Success("ok")
        )

        val result = useCase.handle("playlist1", "track1")

        coVerify(exactly = 2) { addItemToPlaylistUseCase(any(), any()) }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `should retry twice and succeed`() = runTest {
        coEvery { addItemToPlaylistUseCase(any(), any()) } returnsMany listOf(
            NetworkResult.Error("fail"),
            NetworkResult.Error("fail"),
            NetworkResult.Success("ok")
        )

        val result = useCase.handle("playlist1", "track1")

        coVerify(exactly = 3) { addItemToPlaylistUseCase(any(), any()) }
        assertTrue(result is NetworkResult.Success)
    }

    @Test
    fun `should fail after 3 attempts`() = runTest {
        coEvery { addItemToPlaylistUseCase(any(), any()) } returns NetworkResult.Error("fail")

        val result = useCase.handle("playlist1", "track1")

        coVerify(exactly = 3) { addItemToPlaylistUseCase(any(), any()) }
        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `should not exceed 3 attempts`() = runTest {
        coEvery { addItemToPlaylistUseCase(any(), any()) } returns NetworkResult.Error("fail")

        useCase.handle("playlist1", "track1")

        coVerify(exactly = 3) { addItemToPlaylistUseCase(any(), any()) }
    }

    @Test
    fun `should complete within 3 seconds`() = runTest {
        coEvery { addItemToPlaylistUseCase(any(), any()) } returns NetworkResult.Success("ok")

        val start = System.currentTimeMillis()
        useCase.handle("playlist1", "track1")
        val end = System.currentTimeMillis()

        assertTrue("El test tardó demasiado", (end - start) < 3000)
    }

    @Test
    fun `should return last error`() = runTest {
        coEvery { addItemToPlaylistUseCase(any(), any()) } returnsMany listOf(
            NetworkResult.Error("error 1"),
            NetworkResult.Error("error 2"),
            NetworkResult.Error("final")
        )

        val result = useCase.handle("playlist1", "track1")

        assertEquals("final", (result as NetworkResult.Error).message)
    }

    @Test
    fun `should handle empty track id`() = runTest {
        coEvery { addItemToPlaylistUseCase(any(), "") } returns NetworkResult.Error("invalid")

        val result = useCase.handle("playlist1", "")

        assertTrue(result is NetworkResult.Error)
    }

    @Test
    fun `should always call underlying use case`() = runTest {
        coEvery { addItemToPlaylistUseCase(any(), any()) } returns NetworkResult.Success("ok")

        useCase.handle("playlist1", "track1")

        coVerify(atLeast = 1) { addItemToPlaylistUseCase("playlist1", "track1") }
    }
}
