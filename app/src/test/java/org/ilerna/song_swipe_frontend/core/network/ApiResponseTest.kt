package org.ilerna.song_swipe_frontend.core.network

import io.mockk.every
import io.mockk.mockk
import org.ilerna.song_swipe_frontend.core.network.interceptors.ForbiddenException
import org.ilerna.song_swipe_frontend.core.network.interceptors.HttpException
import org.ilerna.song_swipe_frontend.core.network.interceptors.NotFoundException
import org.ilerna.song_swipe_frontend.core.network.interceptors.ServerException
import org.ilerna.song_swipe_frontend.core.network.interceptors.TooManyRequestsException
import org.ilerna.song_swipe_frontend.core.network.interceptors.UnauthorizedException
import org.junit.Test
import retrofit2.Response
import java.io.IOException
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

    // ==================== Typed-exception code preservation ====================

    @Test
    fun `create maps UnauthorizedException to 401`() {
        val result = ApiResponse.create<String>(UnauthorizedException("expired"))
        assertTrue(result is ApiResponse.Error)
        assertEquals(401, result.code)
    }

    @Test
    fun `create maps ForbiddenException to 403`() {
        val result = ApiResponse.create<String>(ForbiddenException("nope"))
        assertTrue(result is ApiResponse.Error)
        assertEquals(403, result.code)
    }

    @Test
    fun `create maps NotFoundException to 404`() {
        val result = ApiResponse.create<String>(NotFoundException("missing"))
        assertTrue(result is ApiResponse.Error)
        assertEquals(404, result.code)
    }

    @Test
    fun `create maps TooManyRequestsException to 429`() {
        val result = ApiResponse.create<String>(TooManyRequestsException("slow down"))
        assertTrue(result is ApiResponse.Error)
        assertEquals(429, result.code)
    }

    @Test
    fun `create maps ServerException to 500`() {
        val result = ApiResponse.create<String>(ServerException("boom"))
        assertTrue(result is ApiResponse.Error)
        assertEquals(500, result.code)
    }

    @Test
    fun `create maps generic HttpException to its code`() {
        val result = ApiResponse.create<String>(HttpException(418, "teapot"))
        assertTrue(result is ApiResponse.Error)
        assertEquals(418, result.code)
    }

    @Test
    fun `create maps unknown IOException to -1`() {
        val result = ApiResponse.create<String>(IOException("no network"))
        assertTrue(result is ApiResponse.Error)
        assertEquals(-1, result.code)
    }
}
