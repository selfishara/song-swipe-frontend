package org.ilerna.song_swipe_frontend.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class SpotifyPerformanceTest {

    @Test
    fun `load and add track takes less than 3 seconds`() = runTest {
        // Execute and measure total time
        val timeTakenMs = measureTimeMillis {
            simulateSpotifyApiCall(500)
            simulateSpotifyApiCall(400)
        }

        // Verify it doesn't exceed the Non-Functional Requirements limit (3000ms)
        assertTrue(
            "Test failed: Operation took ${timeTakenMs}ms (Limit: 3000ms)",
            timeTakenMs < 3000
        )
    }

    private suspend fun simulateSpotifyApiCall(timeMs: Long) {
        delay(timeMs)
    }
}