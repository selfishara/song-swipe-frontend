package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.PreviewRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetTrackPreviewUseCaseTest {

    private lateinit var previewRepository: PreviewRepository
    private lateinit var useCase: GetTrackPreviewUseCase

    @Before
    fun setup() {
        previewRepository = mockk()
        useCase = GetTrackPreviewUseCase(previewRepository)
    }

    @Test
    fun `returns preview url when repository succeeds`() = runTest {
        val trackName = "Blinding Lights"
        val artistName = "The Weeknd"
        val expectedUrl = "https://cdns-preview-a.dzcdn.net/sample.mp3"

        coEvery {
            previewRepository.getPreviewUrl(trackName, artistName)
        } returns NetworkResult.Success(expectedUrl)

        val result = useCase(trackName, artistName)

        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedUrl, (result as NetworkResult.Success).data)

    }
}