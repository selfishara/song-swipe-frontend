package org.ilerna.song_swipe_frontend.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider
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
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.PlaylistDetailsScreen
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.PlaylistListViewModel
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.PlaylistViewModel
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.PlaylistsScreen
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeScreen
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeViewModel
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeViewModelFactory
import org.ilerna.song_swipe_frontend.presentation.screen.vibe.VibeSelectionScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    @Suppress("UNUSED_PARAMETER") user: User?,
    getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
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
    modifier: Modifier = Modifier
) {
    val genrePlaylistProvider = remember { GenrePlaylistProvider() }

    val swipeViewModel: SwipeViewModel = viewModel(
        factory = SwipeViewModelFactory(
            getPlaylistTracksUseCase = getPlaylistTracksUseCase,
            getTrackPreviewUseCase = getTrackPreviewUseCase,
            processSwipeLikeUseCase = processSwipeLikeUseCase,
            recordSkipUseCase = recordSkipUseCase,
            getSkippedTrackIdsUseCase = getSkippedTrackIdsUseCase,
            getUserPlaylistsUseCase = getUserPlaylistsUseCase,
            getActivePlaylistUseCase = getActivePlaylistUseCase,
            setActivePlaylistUseCase = setActivePlaylistUseCase,
            swipeSessionDataStore = swipeSessionDataStore,
            genrePlaylistProvider = genrePlaylistProvider
        )
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Vibe.route,
        modifier = modifier
    ) {
        composable(route = Screen.Vibe.route) {
            VibeSelectionScreen(
                activeGenre = swipeViewModel.activeGenre,
                onContinueClick = { genre ->
                    swipeViewModel.startSession(genre)

                    navController.navigate(Screen.Swipe.createRoute()) {
                        popUpTo(Screen.Vibe.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

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
                        popUpTo(Screen.Vibe.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Screen.Playlists.route) {
            val playlistListViewModel = remember(
                getUserPlaylistsUseCase,
                getActivePlaylistUseCase,
                setActivePlaylistUseCase,
                createPlaylistUseCase,
                spotifyUserId
            ) {
                PlaylistListViewModel(
                    getUserPlaylistsUseCase = getUserPlaylistsUseCase,
                    getActivePlaylistUseCase = getActivePlaylistUseCase,
                    setActivePlaylistUseCase = setActivePlaylistUseCase,
                    createPlaylistUseCase = createPlaylistUseCase,
                    spotifyUserId = spotifyUserId
                )
            }

            PlaylistsScreen(
                viewModel = playlistListViewModel,
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetails.createRoute(playlistId))
                }
            )
        }

        composable(
            route = Screen.PlaylistDetails.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Screen.PlaylistDetails.ARG_PLAYLIST_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments
                ?.getString(Screen.PlaylistDetails.ARG_PLAYLIST_ID).orEmpty()

            val playlistViewModel = remember(
                getPlaylistTracksUseCase,
                removeItemFromPlaylistUseCase
            ) {
                PlaylistViewModel(
                    getPlaylistTracksUseCase = getPlaylistTracksUseCase,
                    removeItemFromPlaylistUseCase = removeItemFromPlaylistUseCase
                )
            }

            PlaylistDetailsScreen(
                viewModel = playlistViewModel,
                playlistId = playlistId
            )
        }
    }
}