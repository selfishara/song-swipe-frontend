package org.ilerna.song_swipe_frontend.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design tokens specific to the Swipe feature.
 *
 * These values are intentionally isolated from the global theme system
 * because the Swipe mock uses very specific dimensions, colors and rotations.
 */
object SwipeColors {
    // Gradient background
    val GradientTop = Color(0xFFB15A4E)
    val GradientMid = Color(0xFF7A3D7E)
    val GradientBottom = Color(0xFF2F93B8)

    // Back cards
    val BackCardBlue = Color(0xFF3D86B5)
    val BackCardPink = Color(0xFFCE4D7E)

    // Main card
    val MainCard = Color(0xFF6B2A33)

    // Header
    val Title = Color(0xFFFF7A8A)

    // Bottom buttons
    val Dislike = Color(0xFFFFC857)
    val Like = Color(0xFFFF2BD6)
}

/**
 * Dimension tokens for Swipe screen mock layout.
 *
 * Not reused from global Spacing/Sizes to avoid polluting the design system
 * with feature-specific values.
 */
object SwipeDimens {
    // Screen spacing
    val TitleTopPadding = 18.dp
    val ScreenHorizontalPadding = 24.dp
    val BottomButtonsPadding = 70.dp

    // Cards sizing
    val CardWidth = 260.dp
    val CardHeight = 340.dp
    val CardRadius = 18.dp

    // Backdrop rotation
    const val BackCardLeftRotation = -10f
    const val BackCardRightRotation = 8f
    const val BackCardAlpha = 0.7f

    // Card content
    val CardInnerPadding = 18.dp
    val CoverSize = 190.dp
    val CoverRadius = 10.dp

    val SpacerS = 4.dp
    val SpacerM = 14.dp
    val SpacerL = 16.dp

    // Play button
    val PlayButtonSize = 44.dp
}
