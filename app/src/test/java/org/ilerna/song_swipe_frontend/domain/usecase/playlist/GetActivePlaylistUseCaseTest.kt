package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ActivePlaylistDataStore
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for [GetActivePlaylistUseCase].
 *
 * Verifies the reactive flow accessors and synchronous reads.
 */
class GetActivePlaylistUseCaseTest {

    private lateinit var activePlaylistDataStore: ActivePlaylistDataStore
    private lateinit var useCase: GetActivePlaylistUseCase

    @Before
    fun setUp() {
        activePlaylistDataStore = mockk()
        useCase = GetActivePlaylistUseCase(activePlaylistDataStore)
    }

    @Test
    fun `id returns flow from DataStore`() = runTest {
        // Given
        every { activePlaylistDataStore.activePlaylistId } returns flowOf("playlist-42")

        // When
        val value = useCase.id().first()

        // Then
        assertEquals("playlist-42", value)
    }

    @Test
    fun `id returns null when no playlist saved`() = runTest {
        // Given
        every { activePlaylistDataStore.activePlaylistId } returns flowOf(null)

        // When
        val value = useCase.id().first()

        // Then
        assertNull(value)
    }

    @Test
    fun `name returns flow from DataStore`() = runTest {
        // Given
        every { activePlaylistDataStore.activePlaylistName } returns flowOf("My Favorites")

        // When
        val value = useCase.name().first()

        // Then
        assertEquals("My Favorites", value)
    }

    @Test
    fun `name returns null when no playlist saved`() = runTest {
        // Given
        every { activePlaylistDataStore.activePlaylistName } returns flowOf(null)

        // When
        val value = useCase.name().first()

        // Then
        assertNull(value)
    }

    @Test
    fun `idSync delegates to DataStore getActivePlaylistIdSync`() = runTest {
        // Given
        coEvery { activePlaylistDataStore.getActivePlaylistIdSync() } returns "playlist-sync"

        // When
        val value = useCase.idSync()

        // Then
        assertEquals("playlist-sync", value)
    }

    @Test
    fun `nameSync delegates to DataStore getActivePlaylistNameSync`() = runTest {
        // Given
        coEvery { activePlaylistDataStore.getActivePlaylistNameSync() } returns "Sync Name"

        // When
        val value = useCase.nameSync()

        // Then
        assertEquals("Sync Name", value)
    }

    @Test
    fun `idSync returns null when unset`() = runTest {
        // Given
        coEvery { activePlaylistDataStore.getActivePlaylistIdSync() } returns null

        // When
        val value = useCase.idSync()

        // Then
        assertNull(value)
    }
}
