package org.ilerna.song_swipe_frontend.presentation.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.UserProfileState
import org.ilerna.song_swipe_frontend.domain.usecase.LoginUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.user.GetSpotifyUserProfileUseCase

/**
 * ViewModel for handling login screen state and business logic
 * Updated to support Supabase OAuth flow and Spotify profile fetching
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val getSpotifyUserProfileUseCase: GetSpotifyUserProfileUseCase? = null,

    // Firebase manager used to log events and errors for tracking login behavior
    private val analyticsManager: AnalyticsManager?,

    /**
     * Reactive stream of the current Spotify access token. When this emits null
     * while the user is in AuthState.Success, we treat it as a forced sign-out
     * (e.g. the SpotifyAuthenticator failed to refresh the token).
     */
    private val spotifyAccessTokenFlow: Flow<String?>? = null,

    /**
     * Clears local session-scoped DataStores (swipe session, active playlist).
     * Theme settings are intentionally preserved.
     */
    private val clearSessionDataStores: (suspend () -> Unit)? = null
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userProfileState = MutableStateFlow<UserProfileState>(UserProfileState.Idle)
    val userProfileState: StateFlow<UserProfileState> = _userProfileState.asStateFlow()

    init {
        // Check for existing session on initialization
        checkExistingSession()
        observeForcedLogout()
    }

    /**
     * Observes the Spotify access-token flow and reacts when it transitions
     * to null while the user is currently authenticated. The transition can
     * only be triggered by SpotifyTokenHolder.clear(), which the
     * SpotifyAuthenticator calls after a terminal 401.
     */
    private fun observeForcedLogout() {
        val flow = spotifyAccessTokenFlow ?: return
        viewModelScope.launch {
            flow.collect { token ->
                if (token.isNullOrEmpty() && _authState.value is AuthState.Success) {
                    forceSignOut()
                }
            }
        }
    }

    private suspend fun forceSignOut() {
        clearAuthState()
    }

    /**
     * Performs the shared sign-out work: clears the Supabase session, drops
     * session-scoped DataStores, and resets the in-memory auth state. Theme
     * settings are preserved.
     */
    private suspend fun clearAuthState() {
        try {
            loginUseCase.signOut()
        } catch (_: Exception) {
            // best-effort: even if Supabase fails we still want to drop local state
        }
        try {
            clearSessionDataStores?.invoke()
        } catch (_: Exception) {
            // ignore
        }
        _authState.value = AuthState.Idle
        _userProfileState.value = UserProfileState.Idle
    }

    /**
     * Checks if there's an existing session and updates the state accordingly
     */
    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                // Wait for Supabase to finish initialization and load session from storage
                loginUseCase.awaitInitialization()

                if (loginUseCase.hasActiveSession()) {
                    val user = loginUseCase.getCurrentUser()
                    if (user != null) {
                        _authState.value = AuthState.Success(user.id)
                        // Fetch Spotify profile after successful auth
                        fetchSpotifyUserProfile()
                    } else {
                        _authState.value = AuthState.Idle
                    }
                } else {
                    _authState.value = AuthState.Idle
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Idle
            }
        }
    }

    /**
     * Initiates the Spotify login flow via Supabase
     * Supabase will automatically open the browser
     */
    fun initiateLogin() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Track that the Spotify login process has started, for analytics purposes
                analyticsManager?.logSpotifyLoginStart()

                loginUseCase.initiateLogin()
                // Browser opens automatically, state will update on callback
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")


            }
        }
    }

    /**
     * Handles the authentication callback from Supabase
     * @param url The deep link URL containing session tokens
     */
    fun handleAuthCallback(url: String) {
        viewModelScope.launch {
            try {
                val result = loginUseCase.handleAuthResponse(url)
                _authState.value = result

                // If authentication was successful, fetch Spotify profile
                if (result is AuthState.Success) {
                    // Track that the Spotify login process completed successfully
                    analyticsManager?.logSpotifyLoginSuccess()

                    fetchSpotifyUserProfile()
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
                //track failed Spotify login
                analyticsManager?.logSpotifyLoginError(e)

            }
        }
    }

    /**
     * Fetches the user's Spotify profile after successful authentication
     * Updates userProfileState with the result
     */
    private fun fetchSpotifyUserProfile() {
        // Only fetch if use case is available
        val useCase = getSpotifyUserProfileUseCase ?: return

        viewModelScope.launch {
            _userProfileState.value = UserProfileState.Loading

            when (val result = useCase()) {
                is NetworkResult.Success -> {
                    _userProfileState.value = UserProfileState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _userProfileState.value = UserProfileState.Error(
                        result.message
                    )
                }
                is NetworkResult.Loading -> {
                    // Already set to loading above
                }
            }
        }
    }

    /**
     * Resets the authentication state
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
        _userProfileState.value = UserProfileState.Idle
    }

    /**
     * Signs out the user and clears the session
     */
    fun signOut() {
        viewModelScope.launch {
            clearAuthState()
        }
    }
}