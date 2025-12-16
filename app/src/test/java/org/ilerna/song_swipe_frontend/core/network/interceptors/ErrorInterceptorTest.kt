package org.ilerna.song_swipe_frontend.core.network.interceptors

import android.util.Log
import io.mockk.*
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Unit tests for ErrorInterceptor
 * Focuses on error detection, logging, and exception throwing
 */
class ErrorInterceptorTest {

    private lateinit var interceptor: ErrorInterceptor
    private lateinit var mockChain: Interceptor.Chain

    @Before
    fun setup() {
        // Mock Android Log to prevent "Method not mocked" errors
        mockkStatic(Log::class)
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        interceptor = ErrorInterceptor()
        mockChain = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    // ==================== Successful Response Tests ====================

    @Test
    fun `intercept should proceed normally for successful response`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val successResponse = mockk<Response> {
            every { isSuccessful } returns true
            every { code } returns 200
        }

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns successResponse

        // When
        val result = interceptor.intercept(mockChain)

        // Then
        assertEquals(successResponse, result)
        verify(exactly = 0) { Log.e(any(), any<String>()) }
    }

    // ==================== HTTP Error Tests ====================

    @Test
    fun `intercept should throw UnauthorizedException for 401 response`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val errorBody = """{"error": {"message": "Invalid token"}}"""
            .toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = mockk<Response> {
            every { isSuccessful } returns false
            every { code } returns 401
            every { message } returns "Unauthorized"
            every { body } returns errorBody
            every { this@mockk.request } returns request
        }

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns errorResponse

        // When/Then
        val exception = assertFailsWith<UnauthorizedException> {
            interceptor.intercept(mockChain)
        }
        assertEquals("Invalid or expired token", exception.message)
    }

    @Test
    fun `intercept should throw ForbiddenException for 403 response`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val errorResponse = mockk<Response> {
            every { isSuccessful } returns false
            every { code } returns 403
            every { message } returns "Forbidden"
            every { body } returns "".toResponseBody()
            every { this@mockk.request } returns request
        }

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns errorResponse

        // When/Then
        val exception = assertFailsWith<ForbiddenException> {
            interceptor.intercept(mockChain)
        }
        assertEquals("You don't have permission for this action", exception.message)
    }

    @Test
    fun `intercept should throw NotFoundException for 404 response`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/tracks/invalid_id")
            .build()
        val errorResponse = mockk<Response> {
            every { isSuccessful } returns false
            every { code } returns 404
            every { message } returns "Not Found"
            every { body } returns "".toResponseBody()
            every { this@mockk.request } returns request
        }

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns errorResponse

        // When/Then
        val exception = assertFailsWith<NotFoundException> {
            interceptor.intercept(mockChain)
        }
        assertEquals("Resource not found", exception.message)
    }

    @Test
    fun `intercept should throw TooManyRequestsException for 429 response`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val errorResponse = mockk<Response> {
            every { isSuccessful } returns false
            every { code } returns 429
            every { message } returns "Too Many Requests"
            every { body } returns "".toResponseBody()
            every { this@mockk.request } returns request
        }

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns errorResponse

        // When/Then
        val exception = assertFailsWith<TooManyRequestsException> {
            interceptor.intercept(mockChain)
        }
        assertEquals("Too many requests", exception.message)
    }

    @Test
    fun `intercept should throw ServerException for 500 response`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val errorBody = """{"error": {"message": "Internal Server Error"}}"""
            .toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = mockk<Response> {
            every { isSuccessful } returns false
            every { code } returns 500
            every { message } returns "Internal Server Error"
            every { body } returns errorBody
            every { this@mockk.request } returns request
        }

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns errorResponse

        // When/Then
        val exception = assertFailsWith<ServerException> {
            interceptor.intercept(mockChain)
        }
        assertTrue(exception.message?.contains("Server error") == true)
    }

    @Test
    fun `intercept should throw HttpException for other error codes`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val errorResponse = mockk<Response> {
            every { isSuccessful } returns false
            every { code } returns 418
            every { message } returns "I'm a teapot"
            every { body } returns "".toResponseBody()
            every { this@mockk.request } returns request
        }

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns errorResponse

        // When/Then
        val exception = assertFailsWith<HttpException> {
            interceptor.intercept(mockChain)
        }
        assertEquals(418, exception.code)
        assertTrue(exception.message?.contains("418") == true)
    }

    // ==================== Network Exception Tests ====================

    @Test
    fun `intercept should throw NetworkException for connection errors`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } throws java.net.SocketTimeoutException("Timeout")

        // When/Then
        val exception = assertFailsWith<NetworkException> {
            interceptor.intercept(mockChain)
        }
        assertTrue(exception.message?.contains("Connection error") == true)
    }

    // ==================== Error Parsing Tests ====================

    @Test
    fun `intercept should parse Spotify error JSON format`() {
        // Given
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()
        val spotifyErrorJson = """{"error": {"status": 401, "message": "The access token expired"}}"""
            .toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = mockk<Response> {
            every { isSuccessful } returns false
            every { code } returns 401
            every { message } returns "Unauthorized"
            every { body } returns spotifyErrorJson
            every { this@mockk.request } returns request
        }

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns errorResponse

        // When/Then
        assertFailsWith<UnauthorizedException> {
            interceptor.intercept(mockChain)
        }
        
        // Verify that error was logged with parsed message
        verify { Log.e("ErrorInterceptor", match { it.contains("The access token expired") }) }
    }
}
