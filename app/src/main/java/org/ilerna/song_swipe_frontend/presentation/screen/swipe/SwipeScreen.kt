package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.presentation.components.swipe.SwipeSongCard
import org.ilerna.song_swipe_frontend.presentation.components.swipe.StackedCardsBackdrop
import org.ilerna.song_swipe_frontend.presentation.components.swipe.SwipeBackground
import org.ilerna.song_swipe_frontend.presentation.components.swipe.SwipeButton
import org.ilerna.song_swipe_frontend.presentation.components.player.PlaybackState
import org.ilerna.song_swipe_frontend.presentation.components.player.PreviewAudioPlayer
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeLayout
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.presentation.theme.Radius
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing
import org.ilerna.song_swipe_frontend.presentation.components.buttons.ButtonStyle
import org.ilerna.song_swipe_frontend.presentation.components.buttons.PrimaryButton

@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel,
    onNavigateToVibe: () -> Unit = {}
) {
    val song = viewModel.currentSongOrNull()
    val hasSession = viewModel.hasSession
    val isLoading = viewModel.isLoading

    // Audio player - remembered across recompositions, released on dispose
    val audioPlayer = remember { PreviewAudioPlayer() }
    val playbackState by audioPlayer.playbackState.collectAsState()
    val playbackProgress by audioPlayer.progress.collectAsState()
    var lastAutoPlayedUrl by remember { mutableStateOf<String?>(null) }

    // Release player when leaving the screen
    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

    // Auto-play logic: play the song automatically when the session starts or after a swipe
    LaunchedEffect(song?.id, song?.previewUrl) {
        val url = song?.previewUrl ?: return@LaunchedEffect

        if (url == lastAutoPlayedUrl) return@LaunchedEffect

        lastAutoPlayedUrl = url

        audioPlayer.stop()
        audioPlayer.playOrToggle(url)
    }

    // Update progress periodically while playing
    LaunchedEffect(playbackState) {
        while (playbackState == PlaybackState.PLAYING) {
            audioPlayer.updateProgress()
            delay(250)
        }
    }

    // No active session - show a prompt to pick a genre
    if (!hasSession && !isLoading) {
        NoSessionContent(onNavigateToVibe = onNavigateToVibe)
        return
    }

    // Session is loading
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    SwipeScreenContent(
        song = song,
        nextSongs = viewModel.nextSongs(2),
        playbackState = playbackState,
        playbackProgress = playbackProgress,
        onPlayClick = {
            song?.previewUrl?.let { url -> audioPlayer.playOrToggle(url) }
        },
        onSwipe = { direction ->
            audioPlayer.stop()
            viewModel.onSwipe(direction)
        },
        onPlaylistFinished = onNavigateToVibe
    )
}

@Composable
private fun SwipeScreenContent(
    song: SongUiModel?,
    nextSongs: List<SongUiModel> = emptyList(),
    playbackState: PlaybackState = PlaybackState.IDLE,
    playbackProgress: Float = 0f,
    onPlayClick: () -> Unit = {},
    onSwipe: suspend (SwipeDirection) -> Unit,
    onPlaylistFinished: () -> Unit = {}
) {
    var interactionLocked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Animatables (Float)
    val dragOffsetX = remember { Animatable(0f) }
    val dragRotationZ = remember { Animatable(0f) }

    // real button width in pixels, needed for drag clamping and swipe threshold
    var containerWidthPx by remember { mutableStateOf(0f) }

    // Reset when song changes
    LaunchedEffect(song?.id) {
        dragOffsetX.snapTo(0f)
        dragRotationZ.snapTo(0f)
        interactionLocked = false
    }

    suspend fun animateSwipe(direction: SwipeDirection) {
        if (interactionLocked) return
        interactionLocked = true

        val width = if (containerWidthPx > 0f) containerWidthPx else 1000f
        val targetX = if (direction == SwipeDirection.RIGHT) width * 1.2f else -width * 1.2f
        val targetRot = if (direction == SwipeDirection.RIGHT) 12f else -12f


        dragOffsetX.animateTo(targetX, tween(220))
        dragRotationZ.animateTo(targetRot, tween(220))

            onSwipe(direction)

            dragOffsetX.snapTo(0f)
            dragRotationZ.snapTo(0f)
            interactionLocked = false

    }

    Scaffold { padding ->
        if (song == null) {
            NoSessionContent(
                modifier = Modifier.padding(padding),
                message = "You've swiped through all the songs!",
                onNavigateToVibe = onPlaylistFinished
            )
            return@Scaffold
        }

        SwipeBackground(modifier = Modifier.padding(padding)) {

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SwipeLayout.horizontalPadding)
                    .padding(bottom = 56.dp),
                contentAlignment = Alignment.Center
            ) {
                // width in pixels, used for drag limits and swipe threshold
                containerWidthPx = constraints.maxWidth.toFloat()
                val threshold = containerWidthPx * 0.25f

                StackedCardsBackdrop(nextSongs = nextSongs)

                SwipeSongCard(
                    song = song,
                    playbackState = playbackState,
                    playbackProgress = playbackProgress,
                    onPlayClick = onPlayClick,
                    modifier = Modifier
                        .size(width = Sizes.cardWidth, height = Sizes.cardHeight)
                        .graphicsLayer {

                            translationX = dragOffsetX.value
                            rotationZ = dragRotationZ.value
                        }
                        .pointerInput(song.id, interactionLocked) {
                            if (interactionLocked) return@pointerInput

                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()

                                    val newX = dragOffsetX.value + dragAmount.x
                                    val clamped = newX.coerceIn(-containerWidthPx, containerWidthPx)

                                    scope.launch {
                                        dragOffsetX.snapTo(clamped)
                                        dragRotationZ.snapTo((clamped / containerWidthPx) * 12f)
                                    }
                                },
                                onDragEnd = {
                                    scope.launch {
                                        val finalX = dragOffsetX.value
                                        when {
                                            finalX > threshold -> animateSwipe(SwipeDirection.RIGHT)
                                            finalX < -threshold -> animateSwipe(SwipeDirection.LEFT)
                                            else -> {
                                                dragOffsetX.animateTo(0f, tween(180))
                                                dragRotationZ.animateTo(0f, tween(180))
                                            }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        dragOffsetX.animateTo(0f, tween(180))
                                        dragRotationZ.animateTo(0f, tween(180))
                                    }
                                }
                            )
                        }
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
                ) { scope.launch {
                    animateSwipe(SwipeDirection.LEFT)
                } }

                SwipeButton(
                    direction = SwipeDirection.RIGHT,
                    enabled = !interactionLocked
                ) {  scope.launch {
                    animateSwipe(SwipeDirection.RIGHT)
                } }
            }
        }
    }
}

@Composable
private fun NoSessionContent(
    modifier: Modifier = Modifier,
    message: String = "No playlist selected",
    onNavigateToVibe: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = Spacing.xl)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = "Pick a vibe to start swiping",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.lg))
            PrimaryButton(
                text = "CHOOSE A VIBE",
                onClick = onNavigateToVibe,
                modifier = Modifier.fillMaxWidth(0.7f),
                shape = RoundedCornerShape(Radius.pill),
                style = ButtonStyle.ACTION,
                enabled = true
            )
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
