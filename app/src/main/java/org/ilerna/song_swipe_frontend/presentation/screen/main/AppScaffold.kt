package org.ilerna.song_swipe_frontend.presentation.screen.main

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ThemeMode
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.usecase.GetSkippedTrackIdsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.RecordSkipUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.CreatePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.swipe.ProcessSwipeLikeUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.RemoveItemFromPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.StreamPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.presentation.components.PlaylistPickerBottomSheet
import org.ilerna.song_swipe_frontend.presentation.components.SignOutConfirmationDialog
import org.ilerna.song_swipe_frontend.presentation.components.ThemeSelectionDialog
import org.ilerna.song_swipe_frontend.presentation.components.layout.NavigationDrawerContent
import org.ilerna.song_swipe_frontend.presentation.components.layout.TopAppBar
import org.ilerna.song_swipe_frontend.presentation.navigation.AppNavigation
import org.ilerna.song_swipe_frontend.presentation.navigation.BottomNavigationBar
import org.ilerna.song_swipe_frontend.presentation.navigation.Screen
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager

@Composable
fun AppScaffold(
    user: User?,
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onSignOut: () -> Unit,
    getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    streamPlaylistTracksUseCase: StreamPlaylistTracksUseCase,
    getTrackPreviewUseCase: GetTrackPreviewUseCase,
    getUserPlaylistsUseCase: GetUserPlaylistsUseCase,
    getActivePlaylistUseCase: GetActivePlaylistUseCase,
    setActivePlaylistUseCase: SetActivePlaylistUseCase,
    createPlaylistUseCase: CreatePlaylistUseCase,
    processSwipeLikeUseCase: ProcessSwipeLikeUseCase,
    recordSkipUseCase: RecordSkipUseCase,
    getSkippedTrackIdsUseCase: GetSkippedTrackIdsUseCase,
    removeItemFromPlaylistUseCase: RemoveItemFromPlaylistUseCase,
    swipeSessionDataStore: SwipeSessionDataStore,
    spotifyUserId: String,
    analyticsManager: AnalyticsManager,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val activePlaylistId by getActivePlaylistUseCase.id().collectAsState(initial = null)
    val activePlaylistName by getActivePlaylistUseCase.name().collectAsState(initial = null)
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var pickerPlaylists by remember { mutableStateOf<List<Playlist>>(emptyList()) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = Screen.fromRoute(currentRoute)

    LaunchedEffect(currentScreen) {
        if (currentScreen is Screen.Swipe && pickerPlaylists.isEmpty()) {
            when (val result = getUserPlaylistsUseCase()) {
                is NetworkResult.Success -> pickerPlaylists = result.data
                is NetworkResult.Error -> Log.e("AppScaffold", "Failed to preload playlists: ${result.message}")
                is NetworkResult.Loading -> { }
            }
        }
    }

    val showBottomBar = when {
        currentRoute == Screen.Vibe.route -> true
        currentRoute?.startsWith("swipe") == true -> true
        currentRoute == Screen.Playlists.route -> true
        currentRoute?.startsWith("playlist/") == true -> true
        else -> false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                user = user,
                onOpenSpotify = {
                    val spotifyIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("spotify://")
                        setPackage("com.spotify.music")
                    }
                    if (spotifyIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(spotifyIntent)
                    } else {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"))
                        context.startActivity(webIntent)
                    }
                    scope.launch { drawerState.close() }
                },
                onThemeClick = {
                    scope.launch { drawerState.close() }
                    showThemeDialog = true
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                },
                onSignOut = {
                    scope.launch { drawerState.close() }
                    showSignOutDialog = true
                }
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    user = user,
                    currentScreen = currentScreen,
                    onAvatarClick = {
                        scope.launch { drawerState.open() }
                    },
                    activePlaylistName = if (currentScreen is Screen.Swipe) activePlaylistName else null,
                    onActivePlaylistClick = if (currentScreen is Screen.Swipe) {
                        {
                            if (pickerPlaylists.isNotEmpty()) {
                                showPlaylistPicker = true
                            } else {
                                scope.launch {
                                    when (val result = getUserPlaylistsUseCase()) {
                                        is NetworkResult.Success -> {
                                            pickerPlaylists = result.data
                                            showPlaylistPicker = true
                                        }
                                        is NetworkResult.Error -> Log.e("AppScaffold", "Failed to load playlists: ${result.message}")
                                        is NetworkResult.Loading -> { }
                                    }
                                }
                            }
                        }
                    } else null
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                user = user,
                getPlaylistTracksUseCase = getPlaylistTracksUseCase,
                streamPlaylistTracksUseCase = streamPlaylistTracksUseCase,
                getTrackPreviewUseCase = getTrackPreviewUseCase,
                getUserPlaylistsUseCase = getUserPlaylistsUseCase,
                getActivePlaylistUseCase = getActivePlaylistUseCase,
                setActivePlaylistUseCase = setActivePlaylistUseCase,
                createPlaylistUseCase = createPlaylistUseCase,
                processSwipeLikeUseCase = processSwipeLikeUseCase,
                recordSkipUseCase = recordSkipUseCase,
                getSkippedTrackIdsUseCase = getSkippedTrackIdsUseCase,
                removeItemFromPlaylistUseCase = removeItemFromPlaylistUseCase,
                swipeSessionDataStore = swipeSessionDataStore,
                analyticsManager = analyticsManager,
                spotifyUserId = spotifyUserId,
                analyticsManager = analyticsManager,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = onThemeSelected,
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showSignOutDialog) {
        SignOutConfirmationDialog(
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showPlaylistPicker) {
        PlaylistPickerBottomSheet(
            playlists = pickerPlaylists,
            activePlaylistId = activePlaylistId,
            onPlaylistSelected = { playlist ->
                scope.launch {
                    setActivePlaylistUseCase(
                        playlistId = playlist.id,
                        playlistName = playlist.name
                    )
                }
                showPlaylistPicker = false
            },
            onDismiss = { showPlaylistPicker = false }
        )
    }
}