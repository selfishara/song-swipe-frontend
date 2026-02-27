package org.ilerna.song_swipe_frontend.presentation.theme

import androidx.compose.ui.unit.dp


/**
 * Dimension tokens for Login screen mock layout.
 *
 * Not reused from global Spacing/Sizes to avoid polluting the design system
 * with screen-specific values and to keep changes isolated.
 */
object LoginDimens {
    val ScreenHorizontalPadding = Spacing.xl  // keep consistent with global spacing
    val LogoSize = 220.dp                    // mock-driven
    val LogoToTitleSpacing = Spacing.xl
    val TitleToButtonSpacing = Spacing.xxl
    val DisclaimerTopSpacing = Spacing.lg
}