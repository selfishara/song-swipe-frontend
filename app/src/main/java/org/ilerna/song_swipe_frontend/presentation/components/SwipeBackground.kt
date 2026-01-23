package org.ilerna.song_swipe_frontend.presentation.components

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
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeColors
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeDimens

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
            SwipeColors.GradientTop,
            SwipeColors.GradientMid,
            SwipeColors.GradientBottom
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
 * Purely visual, no interaction logic.
 */
@Composable
fun StackedCardsBackdrop(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = SwipeDimens.CardWidth, height = SwipeDimens.CardHeight)
            .rotate(SwipeDimens.BackCardLeftRotation),
        shape = RoundedCornerShape(SwipeDimens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = SwipeColors.BackCardBlue.copy(alpha = SwipeDimens.BackCardAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {}

    Card(
        modifier = modifier
            .size(width = SwipeDimens.CardWidth, height = SwipeDimens.CardHeight)
            .rotate(SwipeDimens.BackCardRightRotation),
        shape = RoundedCornerShape(SwipeDimens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = SwipeColors.BackCardPink.copy(alpha = SwipeDimens.BackCardAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {}
}
