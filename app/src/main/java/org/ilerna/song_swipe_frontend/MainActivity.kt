package org.ilerna.song_swipe_frontend

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.data.repository.SpotifyAuthRepository
import org.ilerna.song_swipe_frontend.domain.usecase.LoginUseCase
import org.ilerna.song_swipe_frontend.ui.screen.login.LoginScreen
import org.ilerna.song_swipe_frontend.ui.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.ui.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize dependencies (in a real app, use Dependency Injection like Hilt)
        val authRepository = SpotifyAuthRepository(this)
        val loginUseCase = LoginUseCase(authRepository)
        viewModel = LoginViewModel(loginUseCase)
        
        // Check if we're being called back from Spotify auth
        handleIntent(intent)
        
        setContent {
            val authState by viewModel.authState.collectAsState()
            
            SongSwipeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        authState = authState,
                        onLoginClick = { viewModel.initiateLogin() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
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
                viewModel.handleAuthCallback(uri)
            }
        }
    }
}