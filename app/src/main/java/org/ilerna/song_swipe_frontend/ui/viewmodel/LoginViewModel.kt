package org.ilerna.song_swipe_frontend.ui.viewmodel

import android.net.Uri
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
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    /**
     * Initiates the Spotify login flow
     */
    fun initiateLogin() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                loginUseCase.initiateLogin()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    /**
     * Handles the authentication callback from Spotify
     * @param uri The callback URI containing the response
     */
    fun handleAuthCallback(uri: Uri) {
        viewModelScope.launch {
            try {
                val result = loginUseCase.handleAuthResponse(uri)
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
    }
}
