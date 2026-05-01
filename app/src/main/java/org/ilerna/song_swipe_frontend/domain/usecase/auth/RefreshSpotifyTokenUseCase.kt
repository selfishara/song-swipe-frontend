package org.ilerna.song_swipe_frontend.domain.usecase.auth

import org.ilerna.song_swipe_frontend.domain.repository.SpotifyTokenRepository

/**
 * Use case that exchanges the stored Spotify refresh token for a fresh
 * access token. Returns the new access token, or null if the refresh
 * cannot be performed (no refresh token, network failure, 4xx from Spotify).
 *
 * Callers are responsible for deciding what to do on null — typically the
 * OkHttp Authenticator forces a logout when the refresh fails terminally.
 */
class RefreshSpotifyTokenUseCase(
    private val repository: SpotifyTokenRepository
) {
    suspend operator fun invoke(): String? = repository.refreshAccessToken()
}
