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
        // Mockeamos la dependencia que inyectamos
        addItemToDefaultPlaylistUseCase = mockk()
        // Instanciamos la clase real que queremos probar
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

    // Falla total: Agota los 3 intentos y devuelve Error
    @Test
    fun `should fail after 3 attempts`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returns NetworkResult.Error("fail")

        val result = useCase.handle("user1", "spotify1", "track1")

        coVerify(exactly = 3) { addItemToDefaultPlaylistUseCase(any(), any(), any()) }
        assertTrue(result is NetworkResult.Error)
    }

    // No más de 3 intentos: Verificamos el límite estricto
    @Test
    fun `should not exceed 3 attempts`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returns NetworkResult.Error("fail")

        useCase.handle("user1", "spotify1", "track1")

        coVerify(exactly = 3) { addItemToDefaultPlaylistUseCase(any(), any(), any()) }
    }

    // Tiempo: Se completa rápido
    @Test
    fun `should complete within 3 seconds`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returns NetworkResult.Success("ok")

        val start = System.currentTimeMillis()
        useCase.handle("user1", "spotify1", "track1")
        val end = System.currentTimeMillis()

        assertTrue("El test tardó demasiado", (end - start) < 3000)
    }

    // Devuelve último error: Si falla 3 veces, el mensaje debe ser el del último fallo
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

    // Edge case: IDs vacíos
    @Test
    fun `should handle empty track id`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), "") } returns NetworkResult.Error("invalid")

        val result = useCase.handle("user1", "spotify1", "")

        assertTrue(result is NetworkResult.Error)
    }

    // Siempre llama al use case original
    @Test
    fun `should always call underlying use case`() = runTest {
        coEvery { addItemToDefaultPlaylistUseCase(any(), any(), any()) } returns NetworkResult.Success("ok")

        useCase.handle("user1", "spotify1", "track1")

        coVerify(atLeast = 1) { addItemToDefaultPlaylistUseCase("user1", "spotify1", "track1") }
    }
}