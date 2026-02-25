package org.ilerna.song_swipe_frontend.presentation.components.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.presentation.theme.Radius
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeBackdropColors
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeLayout
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeScreenGradient

/**
 * Fullscreen gradient background for the Swipe screen.
 * Matches the visual mock provided for the feed.
 */
@Composable
fun SwipeBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
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
 */
@Composable
fun StackedCardsBackdrop(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = Sizes.cardWidth, height = Sizes.cardHeight)
            .rotate(SwipeLayout.backdropLeftRotation),
        shape = RoundedCornerShape(Radius.large),
        colors = CardDefaults.cardColors(
            containerColor = SwipeBackdropColors.CardBlue.copy(alpha = SwipeLayout.backdropAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {}

    Card(
        modifier = modifier
            .size(width = Sizes.cardWidth, height = Sizes.cardHeight)
            .rotate(SwipeLayout.backdropRightRotation),
        shape = RoundedCornerShape(Radius.large),
        colors = CardDefaults.cardColors(
            containerColor = SwipeBackdropColors.CardPink.copy(alpha = SwipeLayout.backdropAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {}
}
