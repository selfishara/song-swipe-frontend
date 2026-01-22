package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.presentation.components.SongCardMock
import org.ilerna.song_swipe_frontend.presentation.components.StackedCardsBackdrop
import org.ilerna.song_swipe_frontend.presentation.components.SwipeBackground

@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel = viewModel()
) {
    val song = viewModel.currentSongOrNull()

    val snackbarHostState = remember { SnackbarHostState() }
    var interactionLocked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
            // Título arriba (como en mock)
            Text(
                text = "SongSwipe",
                color = Color(0xFFFF7A8A),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp)
            )

            // Centro: stack + card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                StackedCardsBackdrop()

                SongCardMock(
                    song = song,
                    modifier = Modifier.size(width = 260.dp, height = 340.dp)
                )
            }

            // Botones inferiores (X y ❤️ como el mock)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 70.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✕",
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    color = Color(0xFFFFC857), // amarillo
                    modifier = Modifier.clickable(enabled = !interactionLocked) {
                        handleSwipe(SwipeDirection.LEFT)
                    }
                )

                Text(
                    text = "❤",
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    color = Color(0xFFFF2BD6), // rosa
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
