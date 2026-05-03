package org.ilerna.song_swipe_frontend.domain.repository

/**
 * Repository contract for the Spotify OAuth token-refresh flow.
 *
 * Lives in the domain layer to keep the use case framework-free; the
 * implementation delegates to Retrofit + SpotifyTokenHolder in the data layer.
 */
interface SpotifyTokenRepository {

    /**
     * Refreshes the Spotify access token using the stored provider_refresh_token.
     *
     * On success the new access token (and rotated refresh token, if returned)
     * is persisted and returned. On failure the existing tokens are NOT
     * cleared — the caller (typically the OkHttp Authenticator) is responsible
     * for deciding whether to force a logout.
     *
     * @return the new access token, or null if no refresh token is available
     *         or the Spotify accounts API rejected the request.
     */
    suspend fun refreshAccessToken(): String?
}
