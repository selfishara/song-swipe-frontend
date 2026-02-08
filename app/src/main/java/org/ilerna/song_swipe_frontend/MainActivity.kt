package org.ilerna.song_swipe_frontend

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.ilerna.song_swipe_frontend.core.auth.SpotifyTokenHolder
import org.ilerna.song_swipe_frontend.core.network.interceptors.SpotifyAuthInterceptor
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SettingsDataStore
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SpotifyTokenDataStore
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ThemeMode
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.SpotifyDataSourceImpl
import org.ilerna.song_swipe_frontend.data.repository.impl.SpotifyRepositoryImpl
import org.ilerna.song_swipe_frontend.data.repository.impl.SupabaseAuthRepository
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.UserProfileState
import org.ilerna.song_swipe_frontend.domain.usecase.LoginUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.user.GetSpotifyUserProfileUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.login.LoginScreen
import org.ilerna.song_swipe_frontend.presentation.screen.login.LoginViewModel
import org.ilerna.song_swipe_frontend.presentation.screen.main.AppScaffold
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize DataStores
        val spotifyTokenDataStore = SpotifyTokenDataStore(applicationContext)
        settingsDataStore = SettingsDataStore(applicationContext)
        SpotifyTokenHolder.initialize(spotifyTokenDataStore)
        
        // Load persisted tokens into memory cache
        lifecycleScope.launch {
            SpotifyTokenHolder.loadFromDataStore()
        }

        // Initialize dependencies - Future implementation of Dependency Injection (using Hilt)
        // TODO: Refactor dependency injection using Hilt
        //       Current manual DI works but doesn't scale well.
        //       See: di/ folder structure in arquitectura docs
        //       Priority: High (critical for maintainability and testing)

        // Auth dependencies
        val authRepository = SupabaseAuthRepository()
        val loginUseCase = LoginUseCase(authRepository)
        viewModel = LoginViewModel(loginUseCase)

        // Spotify API dependencies
        val spotifyAuthInterceptor = SpotifyAuthInterceptor()
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(spotifyAuthInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val spotifyApi = retrofit.create(SpotifyApi::class.java)
        val spotifyDataSource = SpotifyDataSourceImpl(spotifyApi)
        val spotifyRepository = SpotifyRepositoryImpl(spotifyDataSource)
        val getSpotifyUserProfileUseCase = GetSpotifyUserProfileUseCase(spotifyRepository)

        // Create ViewModel with all dependencies
        viewModel = LoginViewModel(loginUseCase, getSpotifyUserProfileUseCase)

        // Check if we're being called back from Supabase OAuth
        handleIntent(intent)

        setContent {
            val authState by viewModel.authState.collectAsState()
            val userProfileState by viewModel.userProfileState.collectAsState()
            val themeMode by settingsDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            // Extract user from UserProfileState if available
            val user = (userProfileState as? UserProfileState.Success)?.user

            // Resolve dark theme based on ThemeMode preference
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            SongSwipeTheme(darkTheme = isDarkTheme) {
                // Show AppScaffold if authenticated, otherwise show LoginScreen
                when (authState) {
                    is AuthState.Success -> {
                        AppScaffold(
                            user = user,
                            currentTheme = themeMode,
                            onThemeSelected = { selectedTheme ->
                                lifecycleScope.launch {
                                    settingsDataStore.setThemeMode(selectedTheme)
                                }
                            },
                            onSignOut = { viewModel.signOut() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LoginScreen(
                            authState = authState,
                            userProfileState = userProfileState,
                            onLoginClick = { viewModel.initiateLogin() },
                            onResetState = { viewModel.resetAuthState() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
                viewModel.handleAuthCallback(uri.toString())
            }
        }
    }
}