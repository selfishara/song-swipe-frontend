package org.ilerna.song_swipe_frontend.integration

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.testutil.SpotifyTestTokenProvider
import org.junit.Assume
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Base class for API integration tests that hit the real Spotify API.
 *
 * Automatically:
 * - Obtains a fresh access token via [SpotifyTestTokenProvider]
 * - Configures OkHttp + Retrofit pointed at the real Spotify API
 * - Skips the test (via JUnit Assume) if no credentials are available
 *
 * Subclasses can use [spotifyApi] to make real API calls.
 */
abstract class BaseApiIntegrationTest {

    protected lateinit var spotifyApi: SpotifyApi
    protected lateinit var accessToken: String

    @Before
    fun setUpApi() {
        val token = SpotifyTestTokenProvider.getAccessToken()
        Assume.assumeNotNull(
            "Skipping: no Spotify test credentials in local.properties",
            token
        )
        accessToken = token!!

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // Use a simple interceptor that injects the test token directly
        val authInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        spotifyApi = retrofit.create(SpotifyApi::class.java)
    }
}
