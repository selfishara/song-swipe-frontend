package org.ilerna.song_swipe_frontend

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.data.repository.impl.SupabaseAuthRepository
import org.ilerna.song_swipe_frontend.domain.usecase.LoginUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.login.LoginScreen
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.screen.login.LoginViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize dependencies - Future implementation of Dependency Injection (using Hilt)
        val authRepository = SupabaseAuthRepository()
        val loginUseCase = LoginUseCase(authRepository)
        viewModel = LoginViewModel(loginUseCase)
        
        // Check if we're being called back from Supabase OAuth
        handleIntent(intent)
        
        setContent {
            val authState by viewModel.authState.collectAsState()
            
            SongSwipeTheme {
                    LoginScreen(
                        authState = authState,
                        onLoginClick = { viewModel.initiateLogin() },
                        modifier = Modifier.fillMaxSize()
                    )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null) {
            lifecycleScope.launch {
                viewModel.handleAuthCallback(uri.toString())
            }
        }
    }
}