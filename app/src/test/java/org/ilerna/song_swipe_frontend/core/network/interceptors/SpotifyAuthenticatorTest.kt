package org.ilerna.song_swipe_frontend.core.network.interceptors

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.ilerna.song_swipe_frontend.domain.usecase.auth.RefreshSpotifyTokenUseCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for SpotifyAuthenticator
 * Verifies the refresh-or-logout logic on a 401 response.
 */
class SpotifyAuthenticatorTest {

    private lateinit var refreshUseCase: RefreshSpotifyTokenUseCase
    private var clearCalls = 0

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        refreshUseCase = mockk()
        clearCalls = 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun build401Response(priorChain: Int = 0): Response {
        val request = Request.Builder().url("https://api.spotify.com/v1/me").build()
        var response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
        repeat(priorChain) {
            response = Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .priorResponse(response)
                .build()
        }
        return response
    }

    private fun newAuthenticator(): SpotifyAuthenticator =
        SpotifyAuthenticator(
            refreshSpotifyTokenUseCase = refreshUseCase,
            tokenHolderClear = { clearCalls++ }
        )

    @Test
    fun `authenticate returns request with new bearer token on refresh success`() {
        coEvery { refreshUseCase() } returns "new-access-token"
        val authenticator = newAuthenticator()

        val newRequest = authenticator.authenticate(null, build401Response())

        assertNotNull(newRequest)
        assertEquals("Bearer new-access-token", newRequest.header("Authorization"))
        assertEquals(0, clearCalls)
        coVerify(exactly = 1) { refreshUseCase() }
    }

    @Test
    fun `authenticate returns null and clears tokens when refresh fails`() {
        coEvery { refreshUseCase() } returns null
        val authenticator = newAuthenticator()

        val newRequest = authenticator.authenticate(null, build401Response())

        assertNull(newRequest)
        assertEquals(1, clearCalls)
        coVerifyAll { refreshUseCase() }
    }

    @Test
    fun `authenticate gives up and clears tokens on second 401 in a row`() {
        // priorChain=1 means the response already has one prior 401, so
        // responseCount() == 2 and the authenticator should bail without refreshing.
        val authenticator = newAuthenticator()

        val newRequest = authenticator.authenticate(null, build401Response(priorChain = 1))

        assertNull(newRequest)
        assertEquals(1, clearCalls)
        coVerify(exactly = 0) { refreshUseCase() }
    }

    @Test
    fun `authenticate returns null and clears tokens when refresh returns blank token`() {
        coEvery { refreshUseCase() } returns ""
        val authenticator = newAuthenticator()

        val newRequest = authenticator.authenticate(null, build401Response())

        assertNull(newRequest)
        assertEquals(1, clearCalls)
    }
}
