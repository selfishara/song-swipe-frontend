package org.ilerna.song_swipe_frontend.data.repository.impl

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.SpotifyDataSourceImpl
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for SpotifyRepositoryImpl
 * Focuses on ApiResponse to NetworkResult conversion and error handling
 */
class SpotifyRepositoryImplTest {

    private lateinit var repository: SpotifyRepositoryImpl
    private lateinit var mockDataSource: SpotifyDataSourceImpl
    private lateinit var api: SpotifyApi

    @Before
    fun setup() {
        api = mockk()
        mockDataSource = mockk()
        repository = SpotifyRepositoryImpl(api,mockDataSource)
    }

    // ==================== Success Cases ====================

    @Test
    fun `getCurrentUserProfile should return Success when API responds successfully`() = runTest {
        // Given
        val mockDto = SpotifyUserDto(
            id = "spotify123",
            displayName = "Test User",
            email = "test@example.com",
            images = null,
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )
        val apiResponse = ApiResponse.Success(mockDto)
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("spotify123", result.data.id)
        assertEquals("test@example.com", result.data.email)
        assertEquals("Test User", result.data.displayName)
        assertNull(result.data.profileImageUrl)
        assertEquals("spotify123", result.data.spotifyId)
    }

    @Test
    fun `getCurrentUserProfile should map DTO with null displayName to User with id as displayName`() = runTest {
        // Given
        val mockDto = SpotifyUserDto(
            id = "spotify456",
            displayName = null,
            email = "fallback@example.com",
            images = null,
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )
        val apiResponse = ApiResponse.Success(mockDto)
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("spotify456", result.data.displayName)
    }

    @Test
    fun `getCurrentUserProfile should map DTO with null email to User with empty email`() = runTest {
        // Given
        val mockDto = SpotifyUserDto(
            id = "spotify789",
            displayName = "No Email User",
            email = null,
            images = null,
            country = null,
            product = null,
            followers = null,
            uri = null,
            href = null
        )
        val apiResponse = ApiResponse.Success(mockDto)
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals("", result.data.email)
    }

    // ==================== Error Cases ====================

    @Test
    fun `getCurrentUserProfile should return Error when API responds with error`() = runTest {
        // Given
        val apiResponse = ApiResponse.Error(
            code = 401,
            message = "Unauthorized",
            errorBody = null
        )
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Unauthorized", result.message)
        assertEquals(401, result.code)
    }

    @Test
    fun `getCurrentUserProfile should return Error with code when API fails`() = runTest {
        // Given
        val apiResponse = ApiResponse.Error(
            code = 404,
            message = "User not found",
            errorBody = "{\"error\": \"not_found\"}"
        )
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("User not found", result.message)
        assertEquals(404, result.code)
    }

    @Test
    fun `getCurrentUserProfile should return Error with negative code for network errors`() = runTest {
        // Given
        val apiResponse = ApiResponse.Error(
            code = -1,
            message = "Connection error",
            errorBody = null
        )
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Connection error", result.message)
        assertEquals(-1, result.code)
    }

    @Test
    fun `getCurrentUserProfile should return Error when rate limited`() = runTest {
        // Given
        val apiResponse = ApiResponse.Error(
            code = 429,
            message = "Too Many Requests",
            errorBody = null
        )
        coEvery { mockDataSource.getCurrentUserProfile() } returns apiResponse

        // When
        val result = repository.getCurrentUserProfile()

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals("Too Many Requests", result.message)
        assertEquals(429, result.code)
    }
}