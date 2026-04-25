package org.ilerna.song_swipe_frontend.presentation.components.swipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
fun SwipeSongCard(
    song: SongUiModel,
    playbackState: PlaybackState = PlaybackState.IDLE,
    playbackProgress: Float = 0f,
    onPlayClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val cardColor = if (isDark) {
        Color(0xFF1E1B22)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val cardBorderColor = if (isDark) {
        Color.White.copy(alpha = 0.06f)
    } else {
        Color.Black.copy(alpha = 0.06f)
    }
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.large),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 14.dp
        ),
        border = BorderStroke(
            1.dp, cardBorderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier.size(Sizes.coverImage),
                shape = RoundedCornerShape(Radius.small),
                elevation = CardDefaults.cardElevation(defaultElevation = 15.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(song.imageUrl).crossfade(true)
                        .build(),
                    placeholder = painterResource(id = R.drawable.ic_music_placeholder),
                    error = painterResource(id = R.drawable.ic_music_placeholder),
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }


            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = song.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE
                )

            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = song.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE
                )
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Play/Pause button – ring-only design.
            // No background Surface; the CircularProgressIndicator ring
            // acts as the visual boundary of the button. The icon floats inside.
            val strokeWidth = 3.dp
            val ringSize = Sizes.buttonCircle // 64.dp

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(ringSize)
                    .clickable(enabled = song.previewUrl != null) { onPlayClick() }) {
                // Ring is always visible: progress / spinning / idle track
                when (playbackState) {
                    PlaybackState.LOADING -> CircularProgressIndicator(
                        modifier = Modifier.size(ringSize),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = strokeWidth
                    )

                    PlaybackState.PLAYING, PlaybackState.PAUSED -> CircularProgressIndicator(
                        progress = { playbackProgress },
                        modifier = Modifier.size(ringSize),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = strokeWidth
                    )

                    else -> CircularProgressIndicator(
                        progress = { 0f },
                        modifier = Modifier.size(ringSize),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        strokeWidth = strokeWidth
                    )
                }

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
                    tint = if (song.previewUrl != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(Sizes.iconLarge)
                )
            }
        }
    }
}
