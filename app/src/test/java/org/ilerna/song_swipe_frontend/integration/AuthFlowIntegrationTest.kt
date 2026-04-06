package org.ilerna.song_swipe_frontend.integration

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.ilerna.song_swipe_frontend.core.auth.SpotifyTokenHolder
import org.ilerna.song_swipe_frontend.core.network.interceptors.SpotifyAuthInterceptor
import org.ilerna.song_swipe_frontend.testutil.SpotifyTestTokenProvider
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration tests for the authentication / token injection flow.
 *
 * Validates that:
 * - [SpotifyTestTokenProvider] can exchange a refresh token for an access token
 * - [SpotifyAuthInterceptor] injects the Bearer header correctly for real requests
 * - An invalid token results in a 401 from the Spotify API
 */
class AuthFlowIntegrationTest {

    @Before
    fun setUp() {
        Assume.assumeTrue(
            "Skipping: no Spotify test credentials in local.properties",
            SpotifyTestTokenProvider.hasCredentials()
        )
    }

    // -- Token Provider -------------------------------------------------------

    @Test
    fun `token provider returns a non-blank access token`() {
        val token = SpotifyTestTokenProvider.getAccessToken()

        assertTrue(!token.isNullOrBlank(), "Access token should not be blank")
        assertTrue(token.length > 20, "Access token should be a reasonable length")
    }

    @Test
    fun `token provider returns the same cached instance on second call`() {
        val first = SpotifyTestTokenProvider.getAccessToken()
        val second = SpotifyTestTokenProvider.getAccessToken()

        assertEquals(first, second, "Token should be cached across calls")
    }

    // -- Interceptor with real API --------------------------------------------

    @Test
    fun `interceptor injects token and Spotify API accepts it`() = runTest {
        val token = SpotifyTestTokenProvider.getAccessToken()!!

        // Inject the token into SpotifyTokenHolder so the interceptor can read it
        SpotifyTokenHolder.setAccessTokenForTest(token)

        val interceptor = SpotifyAuthInterceptor()
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()

        client.newCall(request).execute().use { response ->
            assertEquals(200, response.code, "Spotify API should accept the token")
        }

        SpotifyTokenHolder.clearForTest()
    }

    // -- Invalid token --------------------------------------------------------

    @Test
    fun `request with invalid token returns 401`() = runTest {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("Authorization", "Bearer invalid_token_abc123")
                    .build()
                chain.proceed(req)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .build()

        client.newCall(request).execute().use { response ->
            assertEquals(401, response.code, "Invalid token should result in 401")
        }
    }
}
