package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response payload for POST https://accounts.spotify.com/api/token
 * with grant_type=refresh_token.
 *
 * Spotify may rotate the refresh token, so [refreshToken] can be null
 * (in which case the existing refresh token remains valid).
 */
data class SpotifyTokenRefreshResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("scope") val scope: String?
)
