package org.ilerna.song_swipe_frontend.data.datasource.remote.api

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTokenRefreshResponseDto
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Spotify Accounts API for OAuth token operations.
 * Base URL: https://accounts.spotify.com/
 *
 * This is a different host from the main Web API (api.spotify.com) and
 * authenticates with HTTP Basic auth using the client_id:client_secret pair
 * rather than a Bearer token, so it MUST use a dedicated OkHttp/Retrofit
 * client that does not include the Spotify Bearer-token interceptors.
 */
interface SpotifyAuthApi {

    /**
     * Exchanges a refresh token for a new access token.
     *
     * @param authorization HTTP Basic header value: "Basic " + Base64(client_id:client_secret)
     * @param grantType Must be "refresh_token"
     * @param refreshToken The provider_refresh_token captured at OAuth callback time
     */
    @FormUrlEncoded
    @POST("api/token")
    suspend fun refreshAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Response<SpotifyTokenRefreshResponseDto>
}
