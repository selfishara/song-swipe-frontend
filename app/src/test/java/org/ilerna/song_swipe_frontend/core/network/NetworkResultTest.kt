package org.ilerna.song_swipe_frontend.core.network

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for NetworkResult
 * Focuses on sealed class state validation
 */
class NetworkResultTest {

    // ==================== Success State ====================

    @Test
    fun `Success should contain data`() {
        // Given
        val testData = "test data"

        // When
        val result = NetworkResult.Success(testData)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(testData, result.data)
    }

    @Test
    fun `Success should work with different data types`() {
        // Given
        val intData = 42
        val listData = listOf("a", "b", "c")

        // When
        val intResult = NetworkResult.Success(intData)
        val listResult = NetworkResult.Success(listData)

        // Then
        assertTrue(intResult is NetworkResult.Success)
        assertEquals(42, intResult.data)
        assertTrue(listResult is NetworkResult.Success)
        assertEquals(listOf("a", "b", "c"), listResult.data)
    }

    // ==================== Error State ====================

    @Test
    fun `Error should contain message`() {
        // Given
        val errorMessage = "Something went wrong"

        // When
        val result = NetworkResult.Error(errorMessage)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(errorMessage, result.message)
        assertEquals(null, result.code)
    }

    @Test
    fun `Error should contain message and code`() {
        // Given
        val errorMessage = "Unauthorized"
        val errorCode = 401

        // When
        val result = NetworkResult.Error(errorMessage, errorCode)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(errorMessage, result.message)
        assertEquals(errorCode, result.code)
    }

    // ==================== Loading State ====================

    @Test
    fun `Loading should be a singleton`() {
        // When
        val loading1 = NetworkResult.Loading
        val loading2 = NetworkResult.Loading

        // Then
        assertTrue(loading1 is NetworkResult.Loading)
        assertTrue(loading2 is NetworkResult.Loading)
        assertEquals(loading1, loading2)
    }
}
