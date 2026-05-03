package org.ilerna.song_swipe_frontend.core.network.interceptors

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.ilerna.song_swipe_frontend.core.auth.SpotifyTokenHolder
import org.ilerna.song_swipe_frontend.core.config.AppConfig
import org.ilerna.song_swipe_frontend.domain.usecase.auth.RefreshSpotifyTokenUseCase

/**
 * OkHttp Authenticator that handles 401 responses from the Spotify Web API
 * by attempting a token refresh exactly once per failed request.
 *
 * On success, it rewrites the Authorization header and returns the request
 * for OkHttp to retry. On failure (or the second 401 in a row) it clears
 * SpotifyTokenHolder, causing LoginViewModel's accessToken-flow listener
 * to flip authState to Idle and redirect the user to the login screen.
 *
 * runBlocking is acceptable here because OkHttp invokes Authenticator on
 * a worker thread and the contract is synchronous.
 */
class SpotifyAuthenticator(
    private val refreshSpotifyTokenUseCase: RefreshSpotifyTokenUseCase,
    private val tokenHolderClear: suspend () -> Unit = { SpotifyTokenHolder.clear() }
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            forceLogout("authenticator already retried once")
            return null
        }

        val newToken = runBlocking { refreshSpotifyTokenUseCase() }
        if (newToken.isNullOrEmpty()) {
            forceLogout("token refresh failed")
            return null
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun forceLogout(reason: String) {
        Log.w(AppConfig.LOG_TAG, "SpotifyAuthenticator forcing logout: $reason")
        runBlocking { tokenHolderClear() }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
