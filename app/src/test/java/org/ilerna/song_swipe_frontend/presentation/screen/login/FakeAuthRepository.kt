package org.ilerna.song_swipe_frontend.presentation.screen.login

import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.repository.AuthRepository

class FakeAuthRepository(
    private val hasSession: Boolean = false,
    private val user: User? = null,
    private val authResult: AuthState = AuthState.Idle,
    private val throwOnCallback: Boolean = false
) : AuthRepository {

    override suspend fun initiateSpotifyLogin() {
        // No-op
    }

    override suspend fun handleAuthCallback(url: String): AuthState {
        if (throwOnCallback) {
            throw RuntimeException("Callback error")
        }
        return authResult
    }

    override suspend fun getCurrentUser(): User? = user

    override suspend fun getSpotifyAccessToken(): String? = null

    override suspend fun signOut() {
        // No-op
    }

    override suspend fun hasActiveSession(): Boolean = hasSession
}