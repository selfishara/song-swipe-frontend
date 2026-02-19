package org.ilerna.song_swipe_frontend.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.PlaylistScreen
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeScreen
import org.ilerna.song_swipe_frontend.presentation.screen.vibe.VibeSelectionScreen

/**
 * Main navigation host for the app.
 * Handles navigation between all screens after authentication.
 *
 * @param navController The NavController to manage navigation
 * @param user The current logged-in user
 * @param modifier Modifier for the NavHost
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    user: User?,
    getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    getTrackPreviewUseCase: GetTrackPreviewUseCase,
    getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase,
    supabaseUserId: String,
    spotifyUserId: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Vibe.route,
        modifier = modifier
    ) {
        // Vibe Screen (Home) - Category selection
        composable(route = Screen.Vibe.route) {
            VibeSelectionScreen(
                onContinueClick = { genre ->
                    // Navigate to swipe screen with selected genre
                    // TODO: Fetch playlist based on genre and pass playlistId
                    navController.navigate(Screen.Swipe.createRoute()) {
                        // Keep Vibe in back stack so bottom nav works correctly
                        popUpTo(Screen.Vibe.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // Swipe Screen - Track swiping session
        composable(
            route = Screen.Swipe.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Screen.Swipe.ARG_PLAYLIST_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString(Screen.Swipe.ARG_PLAYLIST_ID)
            SwipeScreen(
                getPlaylistTracksUseCase = getPlaylistTracksUseCase,
                getTrackPreviewUseCase = getTrackPreviewUseCase,
                getOrCreateDefaultPlaylistUseCase = getOrCreateDefaultPlaylistUseCase,
                supabaseUserId = supabaseUserId,
                spotifyUserId = spotifyUserId
                // TODO: Pass playlistId to ViewModel when implemented
                // playlistId = playlistId
            )
        }

        // Playlists Screen - User's saved playlists
        composable(route = Screen.Playlists.route) {
            PlaylistScreen(
                getOrCreateDefaultPlaylistUseCase = getOrCreateDefaultPlaylistUseCase,
                supabaseUserId = supabaseUserId,
                spotifyUserId = spotifyUserId
            )
        }
    }
}
