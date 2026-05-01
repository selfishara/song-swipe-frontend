package org.ilerna.song_swipe_frontend.data.repository.impl

import android.util.Base64
import android.util.Log
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyAuthApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTokenRefreshResponseDto
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpotifyTokenRepositoryImplTest {

    private lateinit var authApi: SpotifyAuthApi
    private lateinit var holder: SpotifyTokenHolderGateway

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
        mockkStatic(Base64::class)
        every {
            Base64.encodeToString(any<ByteArray>(), Base64.NO_WRAP)
        } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg())
        }
        authApi = mockk()
        holder = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        unmockkStatic(Base64::class)
    }

    private fun newRepo(): SpotifyTokenRepositoryImpl =
        SpotifyTokenRepositoryImpl(
            authApi = authApi,
            clientId = "client-id",
            clientSecret = "client-secret",
            tokenHolder = holder
        )

    @Test
    fun `returns null and skips network when no refresh token is stored`() = runTest {
        every { holder.getRefreshToken() } returns null

        val result = newRepo().refreshAccessToken()

        assertNull(result)
        coVerify(exactly = 0) { authApi.refreshAccessToken(any(), any(), any()) }
    }

    @Test
    fun `returns null when client credentials are missing`() = runTest {
        every { holder.getRefreshToken() } returns "stored-refresh"
        val repo = SpotifyTokenRepositoryImpl(
            authApi = authApi,
            clientId = "",
            clientSecret = "",
            tokenHolder = holder
        )

        val result = repo.refreshAccessToken()

        assertNull(result)
        coVerify(exactly = 0) { authApi.refreshAccessToken(any(), any(), any()) }
    }

    @Test
    fun `success path stores and returns the new access token`() = runTest {
        every { holder.getRefreshToken() } returns "stored-refresh"
        coEvery { holder.setTokens(any(), any()) } just Runs
        coEvery {
            authApi.refreshAccessToken(any(), any(), any())
        } returns Response.success(
            SpotifyTokenRefreshResponseDto(
                accessToken = "fresh-access",
                tokenType = "Bearer",
                expiresIn = 3600,
                refreshToken = "rotated-refresh",
                scope = null
            )
        )

        val result = newRepo().refreshAccessToken()

        assertEquals("fresh-access", result)
        coVerify { holder.setTokens("fresh-access", "rotated-refresh") }
    }

    @Test
    fun `success without rotated refresh token preserves existing one`() = runTest {
        every { holder.getRefreshToken() } returns "stored-refresh"
        coEvery { holder.setTokens(any(), any()) } just Runs
        coEvery {
            authApi.refreshAccessToken(any(), any(), any())
        } returns Response.success(
            SpotifyTokenRefreshResponseDto(
                accessToken = "fresh-access",
                tokenType = "Bearer",
                expiresIn = 3600,
                refreshToken = null,
                scope = null
            )
        )

        val result = newRepo().refreshAccessToken()

        assertEquals("fresh-access", result)
        coVerify { holder.setTokens("fresh-access", "stored-refresh") }
    }

    @Test
    fun `HTTP 400 from Spotify returns null and does not update tokens`() = runTest {
        every { holder.getRefreshToken() } returns "stored-refresh"
        coEvery {
            authApi.refreshAccessToken(any(), any(), any())
        } returns Response.error(
            400,
            okhttp3.ResponseBody.create(null, "{\"error\":\"invalid_grant\"}")
        )

        val result = newRepo().refreshAccessToken()

        assertNull(result)
        coVerify(exactly = 0) { holder.setTokens(any(), any()) }
    }

    @Test
    fun `network exception returns null and does not update tokens`() = runTest {
        every { holder.getRefreshToken() } returns "stored-refresh"
        coEvery {
            authApi.refreshAccessToken(any(), any(), any())
        } throws java.io.IOException("offline")

        val result = newRepo().refreshAccessToken()

        assertNull(result)
        coVerify(exactly = 0) { holder.setTokens(any(), any()) }
    }
}
