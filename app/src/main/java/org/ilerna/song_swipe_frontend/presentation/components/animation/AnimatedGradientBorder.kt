package org.ilerna.song_swipe_frontend.presentation.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import org.ilerna.song_swipe_frontend.presentation.theme.AnimationConstants
import org.ilerna.song_swipe_frontend.presentation.theme.Borders
import org.ilerna.song_swipe_frontend.presentation.theme.NeonGradient
import org.ilerna.song_swipe_frontend.presentation.theme.Radius

/**
 * Reusable animated neon border component.
 */
@Composable
fun AnimatedGradientBorder(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = Borders.medium,
    cornerRadius: Dp = Radius.pill,
    animationDurationMillis: Int = AnimationConstants.borderAnimationDurationMillis,
    animationOffset: Float = AnimationConstants.borderAnimationOffset
) {
    val transition = rememberInfiniteTransition(label = "borderTransition")

    val offset by transition.animateFloat(
        initialValue = 0f, targetValue = animationOffset, animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDurationMillis, easing = LinearEasing
            ), repeatMode = RepeatMode.Reverse
        ), label = "offsetAnim"
    )

    Canvas(modifier = modifier) {
        val brush = Brush.linearGradient(
            colors = NeonGradient + NeonGradient.first(),
            start = Offset(-size.width + offset, 0f),
            end = Offset(offset, size.height)
        )

        val radiusPx = cornerRadius.toPx().coerceAtMost(size.minDimension / 2f)

        drawRoundRect(
            brush = brush,
            style = Stroke(width = strokeWidth.toPx()),
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )
    }
}
