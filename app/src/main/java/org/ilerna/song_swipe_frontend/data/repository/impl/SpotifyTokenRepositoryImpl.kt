package org.ilerna.song_swipe_frontend.data.repository.impl

import android.util.Base64
import android.util.Log
import org.ilerna.song_swipe_frontend.core.auth.SpotifyTokenHolder
import org.ilerna.song_swipe_frontend.core.config.AppConfig
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyAuthApi
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyTokenRepository

/**
 * Default implementation of SpotifyTokenRepository.
 *
 * Performs POST https://accounts.spotify.com/api/token with
 * grant_type=refresh_token, using HTTP Basic auth where the userinfo is
 * Base64(client_id:client_secret). Updates SpotifyTokenHolder on success.
 */
class SpotifyTokenRepositoryImpl(
    private val authApi: SpotifyAuthApi,
    private val clientId: String = AppConfig.SPOTIFY_CLIENT_ID,
    private val clientSecret: String = AppConfig.SPOTIFY_CLIENT_SECRET,
    private val tokenHolder: SpotifyTokenHolderGateway = DefaultSpotifyTokenHolderGateway
) : SpotifyTokenRepository {

    override suspend fun refreshAccessToken(): String? {
        val refreshToken = tokenHolder.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.w(AppConfig.LOG_TAG, "Cannot refresh Spotify token: no refresh token stored")
            return null
        }
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            Log.e(AppConfig.LOG_TAG, "Cannot refresh Spotify token: missing client credentials")
            return null
        }

        return try {
            val basic = "Basic " + Base64.encodeToString(
                "$clientId:$clientSecret".toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            val response = authApi.refreshAccessToken(
                authorization = basic,
                refreshToken = refreshToken
            )
            if (!response.isSuccessful) {
                Log.w(
                    AppConfig.LOG_TAG,
                    "Spotify token refresh failed: HTTP ${response.code()}"
                )
                return null
            }
            val body = response.body() ?: return null
            val newAccessToken = body.accessToken
            val rotatedRefreshToken = body.refreshToken ?: refreshToken
            tokenHolder.setTokens(newAccessToken, rotatedRefreshToken)
            newAccessToken
        } catch (e: Exception) {
            Log.e(AppConfig.LOG_TAG, "Spotify token refresh threw", e)
            null
        }
    }
}

/**
 * Thin seam over the SpotifyTokenHolder singleton to keep the repository
 * unit-testable (the holder is an `object`/singleton so it cannot be mocked
 * directly with MockK without `mockkObject`, and that costs a static mock).
 */
interface SpotifyTokenHolderGateway {
    fun getRefreshToken(): String?
    suspend fun setTokens(accessToken: String?, refreshToken: String?)
}

object DefaultSpotifyTokenHolderGateway : SpotifyTokenHolderGateway {
    override fun getRefreshToken(): String? = SpotifyTokenHolder.getRefreshToken()
    override suspend fun setTokens(accessToken: String?, refreshToken: String?) {
        SpotifyTokenHolder.setTokens(accessToken, refreshToken)
    }
}
