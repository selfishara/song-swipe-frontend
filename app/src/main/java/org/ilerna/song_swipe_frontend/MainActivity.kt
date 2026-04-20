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
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager
import org.ilerna.song_swipe_frontend.core.auth.SpotifyTokenHolder
import org.ilerna.song_swipe_frontend.core.network.interceptors.ErrorInterceptor
import org.ilerna.song_swipe_frontend.core.network.interceptors.SpotifyAuthInterceptor
import org.ilerna.song_swipe_frontend.core.network.interceptors.SpotifyPerformanceInterceptor
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ActivePlaylistDataStore
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SettingsDataStore
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SpotifyTokenDataStore
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ThemeMode
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.DeezerApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.DeezerDataSourceImpl
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.SpotifyDataSourceImpl
import org.ilerna.song_swipe_frontend.data.repository.impl.DeezerPreviewRepositoryImpl
import org.ilerna.song_swipe_frontend.data.repository.impl.PlaylistRepositoryImpl
import org.ilerna.song_swipe_frontend.data.repository.impl.SpotifyRepositoryImpl
import org.ilerna.song_swipe_frontend.data.repository.impl.SupabaseAuthRepository
import org.ilerna.song_swipe_frontend.domain.model.AuthState
import org.ilerna.song_swipe_frontend.domain.model.UserProfileState
import org.ilerna.song_swipe_frontend.domain.usecase.LoginUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.CreatePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.swipe.ProcessSwipeLikeUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.AddItemToPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.RemoveItemFromPlaylistUseCase
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

    private lateinit var analyticsManager: AnalyticsManager

    private lateinit var settingsDataStore: SettingsDataStore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        analyticsManager = AnalyticsManager(this)

        val spotifyTokenDataStore = SpotifyTokenDataStore(applicationContext)
        settingsDataStore = SettingsDataStore(applicationContext)
        SpotifyTokenHolder.initialize(spotifyTokenDataStore)

        lifecycleScope.launch {
            SpotifyTokenHolder.loadFromDataStore()
        }

        // Initialize dependencies - Future implementation of Dependency Injection (using Hilt)
        // TODO: Refactor dependency injection using Hilt

        // Auth dependencies
        val authRepository = SupabaseAuthRepository()
        val loginUseCase = LoginUseCase(authRepository)

        // Spotify API dependencies
        val spotifyAuthInterceptor = SpotifyAuthInterceptor()
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val errorInterceptor = ErrorInterceptor(analyticsManager)
        val performanceInterceptor = SpotifyPerformanceInterceptor(analyticsManager)
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(performanceInterceptor)
            .addInterceptor(org.ilerna.song_swipe_frontend.core.network.interceptors.SpotifyRetryInterceptor())
            .addInterceptor(spotifyAuthInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(errorInterceptor)
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
        val getPlaylistTracksUseCase = GetPlaylistTracksUseCase(spotifyRepository)
        val getUserPlaylistsUseCase = GetUserPlaylistsUseCase(spotifyRepository)

        // Playlist CRUD
        val playlistRepository = PlaylistRepositoryImpl(spotifyApi)
        val createPlaylistUseCase = CreatePlaylistUseCase(playlistRepository)

        // Active playlist (local DataStore)
        val activePlaylistDataStore = ActivePlaylistDataStore(applicationContext)
        val getActivePlaylistUseCase = GetActivePlaylistUseCase(activePlaylistDataStore)
        val setActivePlaylistUseCase = SetActivePlaylistUseCase(activePlaylistDataStore)

        // Track operations (operate on any playlist by ID)
        val addItemToPlaylistUseCase = AddItemToPlaylistUseCase(spotifyRepository)
        val removeItemFromPlaylistUseCase = RemoveItemFromPlaylistUseCase(spotifyRepository)
        val processSwipeLikeUseCase = ProcessSwipeLikeUseCase(addItemToPlaylistUseCase)

        // Deezer API dependencies (public API, no auth needed)
        val deezerRetrofit = Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val deezerApi = deezerRetrofit.create(DeezerApi::class.java)
        val deezerDataSource = DeezerDataSourceImpl(deezerApi)
        val deezerPreviewRepository = DeezerPreviewRepositoryImpl(deezerDataSource)
        val getTrackPreviewUseCase = GetTrackPreviewUseCase(deezerPreviewRepository)

        // Swipe session persistence
        val swipeSessionDataStore = SwipeSessionDataStore(applicationContext)

        viewModel = LoginViewModel(
            loginUseCase = loginUseCase,
            getSpotifyUserProfileUseCase = getSpotifyUserProfileUseCase,
            analyticsManager = analyticsManager
        )

        handleIntent(intent)

        setContent {

            val authState by viewModel.authState.collectAsState()
            val userProfileState by viewModel.userProfileState.collectAsState()
            val themeMode by settingsDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            val user = (userProfileState as? UserProfileState.Success)?.user

            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            SongSwipeTheme(darkTheme = isDarkTheme) {
                val spotifyUserId = user?.spotifyId ?: ""

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
                            getPlaylistTracksUseCase = getPlaylistTracksUseCase,
                            getTrackPreviewUseCase = getTrackPreviewUseCase,
                            getUserPlaylistsUseCase = getUserPlaylistsUseCase,
                            getActivePlaylistUseCase = getActivePlaylistUseCase,
                            setActivePlaylistUseCase = setActivePlaylistUseCase,
                            createPlaylistUseCase = createPlaylistUseCase,
                            processSwipeLikeUseCase = processSwipeLikeUseCase,
                            removeItemFromPlaylistUseCase = removeItemFromPlaylistUseCase,
                            swipeSessionDataStore = swipeSessionDataStore,
                            spotifyUserId = spotifyUserId,
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
