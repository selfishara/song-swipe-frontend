package org.ilerna.song_swipe_frontend.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.R
import org.ilerna.song_swipe_frontend.presentation.components.player.PlaybackState
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel
import org.ilerna.song_swipe_frontend.presentation.theme.Radius
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing

/**
 * Song card used in the Swipe screen.
 *
 * Displays cover image, title, artist and a functional play/pause button
 * with a circular progress indicator for the 30-second audio preview.
 *
 * @param song The song data to display
 * @param playbackState Current playback state (IDLE, LOADING, PLAYING, PAUSED, ERROR)
 * @param playbackProgress Playback progress from 0.0 to 1.0
 * @param onPlayClick Callback triggered when the play/pause button is tapped
 * @param modifier Modifier for the card
 */
@Composable
fun SongCardMock(
    song: SongUiModel,
    playbackState: PlaybackState = PlaybackState.IDLE,
    playbackProgress: Float = 0f,
    onPlayClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cardColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.large),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = Spacing.md)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.placeholder_album),
                contentDescription = "Cover",
                modifier = Modifier
                    .size(Sizes.coverImage)
                    .clip(RoundedCornerShape(Radius.small))
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = song.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = song.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Play/Pause button with progress ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Sizes.buttonCircle)
                    .clickable(enabled = song.previewUrl != null) { onPlayClick() }
            ) {
                // Background progress ring (track)
                if (playbackState == PlaybackState.PLAYING || playbackState == PlaybackState.PAUSED) {
                    CircularProgressIndicator(
                        progress = { playbackProgress },
                        modifier = Modifier.size(Sizes.buttonCircle),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 3.dp
                    )
                }

                // Loading spinner
                if (playbackState == PlaybackState.LOADING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Sizes.buttonCircle),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }

                // Button surface
                Surface(
                    shape = CircleShape,
                    color = if (song.previewUrl != null)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(Sizes.buttonCircle - 8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val iconRes = when (playbackState) {
                            PlaybackState.PLAYING -> android.R.drawable.ic_media_pause
                            else -> android.R.drawable.ic_media_play
                        }
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = when (playbackState) {
                                PlaybackState.PLAYING -> "Pause"
                                else -> "Play"
                            },
                            tint = cardColor
                        )
                    }
                }
            }
        }
    }
}
