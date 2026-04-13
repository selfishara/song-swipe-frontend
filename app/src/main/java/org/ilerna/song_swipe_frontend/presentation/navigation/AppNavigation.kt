package org.ilerna.song_swipe_frontend.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.PlaylistsScreen
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeScreen
import org.ilerna.song_swipe_frontend.presentation.screen.vibe.VibeSelectionScreen
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.AddItemToDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.RemoveItemFromDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.PlaylistViewModel
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeViewModel
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeViewModelFactory

/**
 * Main navigation host for the app.
 * Handles navigation between all screens after authentication.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    user: User?,
    getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    getTrackPreviewUseCase: GetTrackPreviewUseCase,
    getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase,
    spotifyRepository: SpotifyRepository,
    swipeSessionDataStore: SwipeSessionDataStore,
    supabaseUserId: String,
    spotifyUserId: String,
    modifier: Modifier = Modifier
) {
    val genrePlaylistProvider = remember { GenrePlaylistProvider() }

    // Shared SwipeViewModel - lives as long as the NavHost so it survives tab switches
    val addItemToDefaultPlaylistUseCase = remember(getOrCreateDefaultPlaylistUseCase, spotifyRepository) {
        AddItemToDefaultPlaylistUseCase(
            getOrCreateDefaultPlaylistUseCase = getOrCreateDefaultPlaylistUseCase,
            spotifyRepository = spotifyRepository
        )
    }
    val removeItemFromDefaultPlaylistUseCase = remember(getOrCreateDefaultPlaylistUseCase, spotifyRepository) {
        RemoveItemFromDefaultPlaylistUseCase(
            getOrCreateDefaultPlaylistUseCase = getOrCreateDefaultPlaylistUseCase,
            spotifyRepository = spotifyRepository
        )
    }
    val swipeViewModel: SwipeViewModel = viewModel(
        factory = SwipeViewModelFactory(
            getPlaylistTracksUseCase = getPlaylistTracksUseCase,
            getTrackPreviewUseCase = getTrackPreviewUseCase,
            getOrCreateDefaultPlaylistUseCase = getOrCreateDefaultPlaylistUseCase,
            addItemToDefaultPlaylistUseCase = addItemToDefaultPlaylistUseCase,
            swipeSessionDataStore = swipeSessionDataStore,
            supabaseUserId = supabaseUserId,
            spotifyUserId = spotifyUserId
        )
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Vibe.route,
        modifier = modifier
    ) {
        // Vibe Screen (Home) - Category selection
        composable(route = Screen.Vibe.route) {
            VibeSelectionScreen(
                activeGenre = swipeViewModel.activeGenre,
                onContinueClick = { genre ->
                    val playlistId = genrePlaylistProvider.getPrimaryPlaylistIdForGenre(genre)
                        ?: return@VibeSelectionScreen

                    // Start a new swipe session with the selected genre
                    swipeViewModel.startSession(playlistId, genre)

                    navController.navigate(Screen.Swipe.createRoute()) {
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
        ) {
            SwipeScreen(
                viewModel = swipeViewModel,
                onNavigateToVibe = {
                    navController.navigate(Screen.Vibe.route) {
                        popUpTo(Screen.Vibe.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Playlists Screen - User's saved playlists
        composable(route = Screen.Playlists.route) {
            val playlistViewModel = remember(
                getOrCreateDefaultPlaylistUseCase,
                getPlaylistTracksUseCase,
                removeItemFromDefaultPlaylistUseCase
            ) {
                PlaylistViewModel(
                    getPlaylistsByGenreUseCase = null,
                    getOrCreateDefaultPlaylistUseCase = getOrCreateDefaultPlaylistUseCase,
                    getPlaylistTracksUseCase = getPlaylistTracksUseCase,
                    removeItemFromDefaultPlaylistUseCase = removeItemFromDefaultPlaylistUseCase
                )
            }

            PlaylistsScreen(
                viewModel = playlistViewModel,
                supabaseUserId = supabaseUserId,
                spotifyUserId = spotifyUserId
            )
        }
    }
}
