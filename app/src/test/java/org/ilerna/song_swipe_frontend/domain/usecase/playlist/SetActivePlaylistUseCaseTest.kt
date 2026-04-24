package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ActivePlaylistDataStore
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SetActivePlaylistUseCase].
 *
 * Verifies that the use case persists the active playlist correctly and
 * delegates clear operations to the underlying DataStore.
 */
class SetActivePlaylistUseCaseTest {

    private lateinit var activePlaylistDataStore: ActivePlaylistDataStore
    private lateinit var useCase: SetActivePlaylistUseCase

    @Before
    fun setUp() {
        activePlaylistDataStore = mockk(relaxed = true)
        useCase = SetActivePlaylistUseCase(activePlaylistDataStore)
    }

    @Test
    fun `invoke persists playlist id and name`() = runTest {
        // Given
        val playlistId = "playlist-123"
        val playlistName = "My Favorites"
        coEvery { activePlaylistDataStore.saveActivePlaylist(any(), any()) } returns Unit

        // When
        useCase(playlistId, playlistName)

        // Then
        coVerify(exactly = 1) {
            activePlaylistDataStore.saveActivePlaylist(
                playlistId = playlistId,
                playlistName = playlistName
            )
        }
    }

    @Test
    fun `invoke forwards exact values without transformation`() = runTest {
        // Given
        val playlistId = "   spaces-id   "
        val playlistName = "   Name With Spaces   "
        coEvery { activePlaylistDataStore.saveActivePlaylist(any(), any()) } returns Unit

        // When
        useCase(playlistId, playlistName)

        // Then
        coVerify {
            activePlaylistDataStore.saveActivePlaylist(
                playlistId = playlistId,
                playlistName = playlistName
            )
        }
    }

    @Test
    fun `clear delegates to DataStore clear`() = runTest {
        // Given
        coEvery { activePlaylistDataStore.clear() } returns Unit

        // When
        useCase.clear()

        // Then
        coVerify(exactly = 1) { activePlaylistDataStore.clear() }
    }

    @Test
    fun `invoke multiple times persists each call`() = runTest {
        // Given
        coEvery { activePlaylistDataStore.saveActivePlaylist(any(), any()) } returns Unit

        // When
        useCase("pl-1", "Name 1")
        useCase("pl-2", "Name 2")

        // Then
        coVerify(exactly = 1) {
            activePlaylistDataStore.saveActivePlaylist("pl-1", "Name 1")
        }
        coVerify(exactly = 1) {
            activePlaylistDataStore.saveActivePlaylist("pl-2", "Name 2")
        }
    }
}
