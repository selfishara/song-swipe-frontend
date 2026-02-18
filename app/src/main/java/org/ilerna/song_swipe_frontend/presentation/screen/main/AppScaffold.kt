package org.ilerna.song_swipe_frontend.presentation.screen.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.presentation.components.NavigationDrawerContent
import org.ilerna.song_swipe_frontend.presentation.components.SongSwipeTopAppBar
import org.ilerna.song_swipe_frontend.presentation.navigation.AppNavigation
import org.ilerna.song_swipe_frontend.presentation.navigation.BottomNavigationBar
import org.ilerna.song_swipe_frontend.presentation.navigation.Screen

/**
 * Main app scaffold with top bar, drawer, and bottom navigation.
 * This is the main container that hosts authenticated screens
 *
 * Features:
 * - Modal navigation drawer (opened via avatar click)
 * - Dynamic top app bar with user avatar and context-aware title
 * - Bottom navigation bar (hidden on certain screens if needed)
 *
 * @param user The current logged-in user
 * @param onSignOut Callback when user signs out
 * @param onThemeToggle Callback to toggle theme
 * @param navController NavController for managing navigation
 * @param modifier Modifier for the scaffold
 */
@Composable
fun AppScaffold(
    user: User?,
    onSignOut: () -> Unit,
    onThemeToggle: () -> Unit,
    getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    getTrackPreviewUseCase: GetTrackPreviewUseCase,
    getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase,
    supabaseUserId: String,
    spotifyUserId: String,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Get current route to determine screen-specific behavior
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = Screen.fromRoute(currentRoute)

    // Determine if bottom bar should be shown
    val showBottomBar = when {
        currentRoute == Screen.Vibe.route -> true
        currentRoute?.startsWith("swipe") == true -> true
        currentRoute == Screen.Playlists.route -> true
        else -> false
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                user = user,
                onOpenSpotify = {
                    // Open Spotify app or web
                    // TODO: 
                    // - We could redirect to Spotify user's profile
                    // - Consider using a more Spotify look and feel button/icon
                    val spotifyIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("spotify://")
                        setPackage("com.spotify.music")
                    }
                    // Check if Spotify app is installed
                    if (spotifyIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(spotifyIntent)
                    } else {
                        // Fallback to web
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"))
                        context.startActivity(webIntent)
                    }
                    scope.launch { drawerState.close() }
                },
                onThemeClick = {
                    onThemeToggle()
                    scope.launch { drawerState.close() }
                },
                onSettingsClick = {
                    // TODO: Navigate to settings when implemented
                    scope.launch { drawerState.close() }
                },
                onSignOut = {
                    scope.launch { drawerState.close() }
                    onSignOut()
                }
            )
        },
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                SongSwipeTopAppBar(
                    user = user,
                    currentScreen = currentScreen,
                    onAvatarClick = {
                        scope.launch { drawerState.open() }
                    }
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
                getTrackPreviewUseCase = getTrackPreviewUseCase,
                getOrCreateDefaultPlaylistUseCase = getOrCreateDefaultPlaylistUseCase,
                supabaseUserId = supabaseUserId,
                spotifyUserId = spotifyUserId,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
