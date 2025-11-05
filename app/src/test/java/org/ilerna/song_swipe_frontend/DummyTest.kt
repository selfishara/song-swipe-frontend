package org.ilerna.song_swipe_frontend

import org.junit.Test
import org.junit.Assert.*

/**
 * Dummy test to verify that the pipeline is working correctly.
 */
class DummyTest {

    @Test
    fun testBasicAssertion() {
        // Simple test that always passes
        val expected = 5
        val actual = 2 + 3
        assertEquals("The sum should be correct", expected, actual)
    }

    @Test
    fun testStringOperations() {
        val message = "Song Swipe Frontend"
        assertNotNull("The message should not be null", message)
        assertTrue("Should contain 'Frontend'", message.contains("Frontend"))
    }

    @Test
    fun testPipelineWorking() {
        val isPipelineWorking = true
        assertTrue("The pipeline should be working :)", isPipelineWorking)
    }
}

