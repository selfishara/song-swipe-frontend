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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.presentation.components.SongCardMock
import org.ilerna.song_swipe_frontend.presentation.components.StackedCardsBackdrop
import org.ilerna.song_swipe_frontend.presentation.components.SwipeBackground
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeColors
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeDimens

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
    viewModel: SwipeViewModel = viewModel()
) {
    val song = viewModel.currentSongOrNull()

    val snackbarHostState = remember { SnackbarHostState() }
    var interactionLocked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Centralized swipe handler to avoid duplicated logic between buttons/gestures
    fun handleSwipe(direction: SwipeDirection) {
        if (interactionLocked) return
        interactionLocked = true

        scope.launch {
            viewModel.onSwipe(direction)

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
                Text("No hay más canciones")
            }
            return@Scaffold
        }

        SwipeBackground(
            modifier = Modifier.padding(padding)
        ) {
            Text(
                text = "SongSwipe",
                color = SwipeColors.Title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = SwipeDimens.TitleTopPadding)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SwipeDimens.ScreenHorizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                StackedCardsBackdrop()

                SongCardMock(
                    song = song,
                    modifier = Modifier.size(
                        width = SwipeDimens.CardWidth,
                        height = SwipeDimens.CardHeight
                    )
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = SwipeDimens.BottomButtonsPadding)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✕",
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    color = SwipeColors.Dislike,
                    modifier = Modifier.clickable(enabled = !interactionLocked) {
                        handleSwipe(SwipeDirection.LEFT)
                    }
                )

                Text(
                    text = "❤",
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    color = SwipeColors.Like,
                    modifier = Modifier.clickable(enabled = !interactionLocked) {
                        handleSwipe(SwipeDirection.RIGHT)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SwipeScreenPreview() {
    SwipeScreen(viewModel = SwipeViewModel())
}
