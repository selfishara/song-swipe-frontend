package org.ilerna.song_swipe_frontend.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.repository.AuthRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for LoginUseCase
 * Focuses on delegation logic and essential use case behavior
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginUseCaseTest {

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var mockAuthRepository: AuthRepository

    @Before
    fun setup() {
        mockAuthRepository = mockk()
        loginUseCase = LoginUseCase(mockAuthRepository)
    }

    // ==================== Delegation Tests ====================

    @Test
    fun `initiateLogin should delegate to repository`() = runTest {
        // Given
        coEvery { mockAuthRepository.initiateSpotifyLogin() } returns Unit

        // When
        loginUseCase.initiateLogin()

        // Then
        coVerify(exactly = 1) { mockAuthRepository.initiateSpotifyLogin() }
    }

    @Test
    fun `handleAuthResponse should delegate to repository and return success state`() = runTest {
        // Given
        val testUrl = "songswipe://callback#access_token=abc123&refresh_token=xyz789"
        val expectedState = AuthState.Success("user123")
        coEvery { mockAuthRepository.handleAuthCallback(testUrl) } returns expectedState

        // When
        val result = loginUseCase.handleAuthResponse(testUrl)

        // Then
        assertEquals(expectedState, result)
        coVerify(exactly = 1) { mockAuthRepository.handleAuthCallback(testUrl) }
    }

    @Test
    fun `handleAuthResponse should return error state for invalid callback`() = runTest {
        // Given
        val invalidUrl = "songswipe://callback#error=access_denied"
        val expectedState = AuthState.Error("Invalid authentication response")
        coEvery { mockAuthRepository.handleAuthCallback(invalidUrl) } returns expectedState

        // When
        val result = loginUseCase.handleAuthResponse(invalidUrl)

        // Then
        assertTrue(result is AuthState.Error)
        assertEquals("Invalid authentication response", result.message)
        coVerify { mockAuthRepository.handleAuthCallback(invalidUrl) }
    }

    @Test
    fun `getCurrentUser should delegate to repository and return user`() = runTest {
        // Given
        val expectedUser = User(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            profileImageUrl = "https://example.com/avatar.jpg",
            spotifyId = "spotify123"
        )
        coEvery { mockAuthRepository.getCurrentUser() } returns expectedUser

        // When
        val result = loginUseCase.getCurrentUser()

        // Then
        assertEquals(expectedUser, result)
        coVerify(exactly = 1) { mockAuthRepository.getCurrentUser() }
    }

    @Test
    fun `getCurrentUser should return null when no user is authenticated`() = runTest {
        // Given
        coEvery { mockAuthRepository.getCurrentUser() } returns null

        // When
        val result = loginUseCase.getCurrentUser()

        // Then
        assertNull(result)
        coVerify { mockAuthRepository.getCurrentUser() }
    }

    @Test
    fun `hasActiveSession should delegate to repository`() = runTest {
        // Given
        coEvery { mockAuthRepository.hasActiveSession() } returns true

        // When
        val result = loginUseCase.hasActiveSession()

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { mockAuthRepository.hasActiveSession() }
    }

    @Test
    fun `signOut should delegate to repository`() = runTest {
        // Given
        coEvery { mockAuthRepository.signOut() } returns Unit

        // When
        loginUseCase.signOut()

        // Then
        coVerify(exactly = 1) { mockAuthRepository.signOut() }
    }

    // ==================== Business Logic Tests ====================

    @Test
    fun `awaitInitialization should delay for 200ms`() = runTest {
        // When
        val startTime = testScheduler.currentTime
        loginUseCase.awaitInitialization()
        val endTime = testScheduler.currentTime

        // Then - Should have delayed approximately 200ms
        val elapsed = endTime - startTime
        assertTrue(elapsed >= 200, "Expected delay of at least 200ms, but was ${elapsed}ms")
    }
}
