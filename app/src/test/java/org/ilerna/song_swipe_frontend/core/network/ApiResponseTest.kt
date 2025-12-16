package org.ilerna.song_swipe_frontend.core.network

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for ApiResponse
 * Focuses on Response<T> conversion and error handling
 */
class ApiResponseTest {

    // ==================== Success Cases ====================

    @Test
    fun `create should return Success when response is successful with body`() {
        // Given
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns "test data"

        // When
        val result = ApiResponse.create(mockResponse)

        // Then
        assertTrue(result is ApiResponse.Success)
        assertEquals("test data", result.data)
    }

    @Test
    fun `create should return Error when response is successful but body is null`() {
        // Given
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns null
        every { mockResponse.code() } returns 200

        // When
        val result = ApiResponse.create(mockResponse)

        // Then
        assertTrue(result is ApiResponse.Error)
        assertEquals(200, result.code)
        assertEquals("Empty response from server", result.message)
    }

    // ==================== Error Cases ====================

    @Test
    fun `create should return Error when response is not successful`() {
        // Given
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code() } returns 404
        every { mockResponse.message() } returns "Not Found"
        every { mockResponse.errorBody()?.string() } returns "Resource not found"

        // When
        val result = ApiResponse.create(mockResponse)

        // Then
        assertTrue(result is ApiResponse.Error)
        assertEquals(404, result.code)
        assertEquals("Not Found", result.message)
        assertEquals("Resource not found", result.errorBody)
    }

    @Test
    fun `create should handle error response without error body`() {
        // Given
        val mockResponse = mockk<Response<String>>()
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code() } returns 401
        every { mockResponse.message() } returns "Unauthorized"
        every { mockResponse.errorBody()?.string() } returns null

        // When
        val result = ApiResponse.create(mockResponse)

        // Then
        assertTrue(result is ApiResponse.Error)
        assertEquals(401, result.code)
        assertEquals("Unauthorized", result.message)
        assertEquals(null, result.errorBody)
    }

    // ==================== Exception Cases ====================

    @Test
    fun `create should handle exception and return Error`() {
        // Given
        val exception = Exception("Network timeout")

        // When
        val result = ApiResponse.create<String>(exception)

        // Then
        assertTrue(result is ApiResponse.Error)
        assertEquals(-1, result.code)
        assertEquals("Network timeout", result.message)
    }

    @Test
    fun `create should handle exception with null message`() {
        // Given
        val exception = Exception(null as String?)

        // When
        val result = ApiResponse.create<String>(exception)

        // Then
        assertTrue(result is ApiResponse.Error)
        assertEquals(-1, result.code)
        assertEquals("Connection error", result.message)
    }
}
