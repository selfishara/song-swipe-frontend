package org.ilerna.song_swipe_frontend.testutil

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Properties

/**
 * Provides a fresh Spotify access token for API integration tests.
 *
 * Uses the Spotify Token API with the `refresh_token` grant type.
 * Credentials are read from `local.properties` at the project root:
 *   - SPOTIFY_CLIENT_ID_TEST
 *   - SPOTIFY_CLIENT_SECRET_TEST
 *   - SPOTIFY_REFRESH_TOKEN_TEST
 *
 * The token is cached per JVM instance so multiple tests share the same token
 * (valid for 1 hour).
 *
 * @see <a href="https://developer.spotify.com/documentation/web-api/tutorials/refreshing-tokens">Spotify Docs</a>
 */
object SpotifyTestTokenProvider {

    private const val TOKEN_URL = "https://accounts.spotify.com/api/token"

    @Volatile
    private var cachedToken: String? = null

    private val client = OkHttpClient()
    private val gson = Gson()

    private data class TokenResponse(
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("token_type") val tokenType: String,
        @SerializedName("expires_in") val expiresIn: Int,
        @SerializedName("scope") val scope: String?
    )

    /**
     * Returns a valid Spotify access token, refreshing only on the first call.
     * Returns `null` if credentials are missing or the refresh fails.
     */
    fun getAccessToken(): String? {
        cachedToken?.let { return it }

        synchronized(this) {
            cachedToken?.let { return it }

            val props = loadProperties() ?: return null
            val clientId = props.getProperty("SPOTIFY_CLIENT_ID_TEST")
            val clientSecret = props.getProperty("SPOTIFY_CLIENT_SECRET_TEST")
            val refreshToken = props.getProperty("SPOTIFY_REFRESH_TOKEN_TEST")

            if (clientId.isNullOrBlank() || clientSecret.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                println("[SpotifyTestTokenProvider] Missing test credentials in local.properties — skipping API tests")
                return null
            }

            val token = refreshAccessToken(clientId, clientSecret, refreshToken)
            cachedToken = token
            return token
        }
    }

    /**
     * Whether valid test credentials are available.
     */
    fun hasCredentials(): Boolean = getAccessToken() != null

    private fun refreshAccessToken(clientId: String, clientSecret: String, refreshToken: String): String? {
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .build()

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("[SpotifyTestTokenProvider] Token refresh failed: ${response.code} — ${response.body.string()}")
                    return null
                }

                val json = response.body.string()
                val tokenResponse = gson.fromJson(json, TokenResponse::class.java)
                println("[SpotifyTestTokenProvider] Token refreshed successfully (expires in ${tokenResponse.expiresIn}s)")
                tokenResponse.accessToken
            }
        } catch (e: Exception) {
            println("[SpotifyTestTokenProvider] Exception refreshing token: ${e.message}")
            null
        }
    }

    private fun loadProperties(): Properties? {
        // Walk up from the working directory to find local.properties
        val candidates = listOf(
            File("local.properties"),
            File("../local.properties"),
            File("../../local.properties")
        )

        val file = candidates.firstOrNull { it.exists() } ?: run {
            println("[SpotifyTestTokenProvider] local.properties not found")
            return null
        }

        return Properties().apply {
            file.inputStream().use { load(it) }
        }
    }
}
