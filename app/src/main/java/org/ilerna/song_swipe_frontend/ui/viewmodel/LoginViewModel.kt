package org.ilerna.song_swipe_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.usecase.LoginUseCase

/**
 * ViewModel for handling login screen state and business logic
 * Updated to support Supabase OAuth flow
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _oauthUrl = MutableStateFlow<String?>(null)
    val oauthUrl: StateFlow<String?> = _oauthUrl.asStateFlow()
    
    /**
     * Initiates the Spotify login flow via Supabase
     * Generates OAuth URL that should be opened in browser
     */
    fun initiateLogin() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val url = loginUseCase.initiateLogin()
                _oauthUrl.value = url
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
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    /**
     * Resets the authentication state
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
        _oauthUrl.value = null
    }
}
