package org.ilerna.song_swipe_frontend.domain.usecase.user

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for GetSpotifyUserProfileUseCase
 * Tests the use case that fetches user profile from Spotify API
 */
class GetSpotifyUserProfileUseCaseTest {

    private lateinit var useCase: GetSpotifyUserProfileUseCase
    private lateinit var mockSpotifyRepository: SpotifyRepository

    @Before
    fun setup() {
        mockSpotifyRepository = mockk()
        useCase = GetSpotifyUserProfileUseCase(mockSpotifyRepository)
    }

    // ==================== Success Cases ====================

    @Test
    fun `invoke should return success with user when repository succeeds`() = runTest {
        // Given
        val expectedUser = User(
            id = "spotify123",
            email = "test@example.com",
            displayName = "Test User",
            profileImageUrl = "https://example.com/avatar.jpg",
            spotifyId = "spotify123"
        )
        coEvery { mockSpotifyRepository.getCurrentUserProfile() } returns NetworkResult.Success(
            expectedUser
        )

        // When
        val result = useCase()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedUser, result.data)
        coVerify(exactly = 1) { mockSpotifyRepository.getCurrentUserProfile() }
    }

    @Test
    fun `invoke should return success with minimal user data`() = runTest {
        // Given - User with only required fields
        val expectedUser = User(
            id = "spotify123",
            email = "",
            displayName = "spotify123",
            profileImageUrl = null,
            spotifyId = "spotify123"
        )
        coEvery { mockSpotifyRepository.getCurrentUserProfile() } returns NetworkResult.Success(
            expectedUser
        )

        // When
        val result = useCase()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedUser, result.data)
    }

    // ==================== Error Cases ====================

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val errorMessage = "Network error"
        val errorCode = 500
        coEvery { mockSpotifyRepository.getCurrentUserProfile() } returns NetworkResult.Error(
            errorMessage,
            errorCode
        )

        // When
        val result = useCase()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(errorMessage, result.message)
        assertEquals(errorCode, result.code)
    }

    @Test
    fun `invoke should return error with 401 when unauthorized`() = runTest {
        // Given
        coEvery { mockSpotifyRepository.getCurrentUserProfile() } returns NetworkResult.Error(
            "Unauthorized",
            401
        )

        // When
        val result = useCase()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(401, result.code)
    }

    @Test
    fun `invoke should return error without code when code is null`() = runTest {
        // Given
        coEvery { mockSpotifyRepository.getCurrentUserProfile() } returns NetworkResult.Error(
            "Unknown error",
            null
        )

        // When
        val result = useCase()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Unknown error", result.message)
        assertEquals(null, result.code)
    }

    // ==================== Repository Interaction Tests ====================

    @Test
    fun `invoke should call repository exactly once`() = runTest {
        // Given
        coEvery { mockSpotifyRepository.getCurrentUserProfile() } returns NetworkResult.Error(
            "Error",
            null
        )

        // When
        useCase()

        // Then
        coVerify(exactly = 1) { mockSpotifyRepository.getCurrentUserProfile() }
    }
}