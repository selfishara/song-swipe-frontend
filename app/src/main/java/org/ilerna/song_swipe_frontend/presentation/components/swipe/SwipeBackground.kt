package org.ilerna.song_swipe_frontend.presentation.components.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel
import org.ilerna.song_swipe_frontend.presentation.theme.Radius
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeBackdropColors
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeLayout
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeScreenGradient
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeScreenGradientDark

/**
 * Fullscreen gradient background for the Swipe screen.
 * Switches between light and dark gradient variants to match the system theme.
 */
@Composable
fun SwipeBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val gradient = Brush.verticalGradient(
        colors = if (isDark) listOf(
            SwipeScreenGradientDark.Top,
            SwipeScreenGradientDark.Mid,
            SwipeScreenGradientDark.Bottom
        ) else listOf(
            SwipeScreenGradient.Top,
            SwipeScreenGradient.Mid,
            SwipeScreenGradient.Bottom
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
        content = content
    )
}

/**
 * Decorative stacked cards shown behind the main song card.
 * When [nextSongs] is provided, each card displays the upcoming cover art.
 */
@Composable
fun StackedCardsBackdrop(
    nextSongs: List<SongUiModel> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val backdropColors = listOf(
        SwipeBackdropColors.CardBlue,
        SwipeBackdropColors.CardPink
    )
    val rotations = listOf(
        SwipeLayout.backdropLeftRotation,
        SwipeLayout.backdropRightRotation
    )

    // Iterate in reverse so nextSongs[0] (the immediate next song) is drawn last and
    // therefore appears in front of nextSongs[1] (the song after that).
    backdropColors.indices.reversed().forEach { index ->
        val color = backdropColors[index]
        val song = nextSongs.getOrNull(index)
        Card(
            modifier = modifier
                .size(width = Sizes.cardWidth, height = Sizes.cardHeight)
                .rotate(rotations[index]),
            shape = RoundedCornerShape(Radius.large),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = SwipeLayout.backdropAlpha)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            if (song?.imageUrl != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(song.imageUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = null,
                        error = null,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(Radius.large))
                    )
                    // Tinted overlay so the card color still bleeds through
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}
