package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.presentation.components.SongCardMock
import org.ilerna.song_swipe_frontend.presentation.components.StackedCardsBackdrop
import org.ilerna.song_swipe_frontend.presentation.components.SwipeBackground
import org.ilerna.song_swipe_frontend.presentation.components.SwipeButton
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeLayout
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel

/**
 * Main Swipe screen.
 *
 * Responsibilities:
 * - Display current song card
 * - Handle like/dislike interactions
 * - Show feedback via Snackbar
 *
 * Swipe animations and navigation are intentionally out of scope
 * for the current sprint.
 */
@Composable
fun SwipeScreen(
    getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    getTrackPreviewUseCase: GetTrackPreviewUseCase,
    viewModel: SwipeViewModel = viewModel(
        factory = SwipeViewModelFactory(getPlaylistTracksUseCase, getTrackPreviewUseCase)
    )
) {
    val song = viewModel.currentSongOrNull()
    SwipeScreenContent(
        song = song,
        onSwipe = { direction -> viewModel.onSwipe(direction) }
    )
}

@Composable
private fun SwipeScreenContent(
    song: SongUiModel?,
    onSwipe: suspend (SwipeDirection) -> Unit
) {

    val snackbarHostState = remember { SnackbarHostState() }
    var interactionLocked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Centralized swipe handler to avoid duplicated logic between buttons/gestures
    fun handleSwipe(direction: SwipeDirection) {
        if (interactionLocked) return
        interactionLocked = true

        scope.launch {
            onSwipe(direction)

            val message = if (direction == SwipeDirection.RIGHT) {
                "Song has been added"
            } else {
                "Song has been discarded"
            }

            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)

            delay(300)
            interactionLocked = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (song == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No more songs available")
            }
            return@Scaffold
        }

        SwipeBackground(
            modifier = Modifier.padding(padding)
        ) {
            Text(
                text = "SongSwipe",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = SwipeLayout.titleTopPadding)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SwipeLayout.horizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                StackedCardsBackdrop()

                SongCardMock(
                    song = song,
                    modifier = Modifier.size(
                        width = Sizes.cardWidth,
                        height = Sizes.cardHeight
                    )
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = SwipeLayout.bottomButtonsPadding)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SwipeButton(
                    direction = SwipeDirection.LEFT,
                    enabled = !interactionLocked
                ) {
                    handleSwipe(SwipeDirection.LEFT)
                }

                SwipeButton(
                    direction = SwipeDirection.RIGHT,
                    enabled = !interactionLocked
                ) {
                    handleSwipe(SwipeDirection.RIGHT)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SwipeScreenPreview() {
    SwipeScreenContent(
        song = SongUiModel(
            id = "1",
            title = "Preview Song",
            artist = "Preview Artist",
            imageUrl = null

        ), onSwipe = { }
    )
}
