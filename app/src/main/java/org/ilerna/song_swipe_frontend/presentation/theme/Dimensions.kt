package org.ilerna.song_swipe_frontend.presentation.theme

import androidx.compose.ui.unit.dp

// ============================================================================
// Spacing - Standard padding and margin values
// ============================================================================

/**
 * Standard spacing values used throughout the app for padding and margins.
 * Use these instead of hardcoded dp values to maintain consistency.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp
    val xxxl = 56.dp
}

// ============================================================================
// Sizes - Standard component sizes
// ============================================================================

/**
 * Standard size values for common UI elements.
 */
object Sizes {
    // Icons
    val iconSmall = 20.dp
    val iconMedium = 24.dp
    val iconLarge = 32.dp
    val iconXLarge = 64.dp

    // Avatars
    val avatarSmall = 32.dp
    val avatarMedium = 40.dp
    val avatarLarge = 56.dp

    // Buttons
    val buttonHeight = 44.dp
    val buttonCircle = 64.dp

    // Images and logos
    val logoSmall = 64.dp
    val logoMedium = 120.dp
    val logoLarge = 170.dp
    val coverImage = 190.dp

    // Cards
    val cardWidth = 260.dp
    val cardHeight = 340.dp
}

// ============================================================================
// Radius - Corner radius values
// ============================================================================

/**
 * Standard corner radius values for shapes.
 */
object Radius {
    val small = 10.dp
    val medium = 16.dp
    val large = 18.dp
    val pill = 38.dp
}

// ============================================================================
// Borders - Border and stroke widths
// ============================================================================

/**
 * Standard border and stroke width values.
 */
object Borders {
    val thin = 2.dp
    val medium = 3.dp
    val thick = 6.dp
}

// ============================================================================
// Swipe Screen Layout - Screen-specific layout constants
// ============================================================================

/**
 * Layout constants specific to the Swipe screen.
 * These define the visual arrangement that matches the mockup design.
 */
object SwipeLayout {
    // Screen margins
    val titleTopPadding = 18.dp
    val horizontalPadding = Spacing.lg
    val bottomButtonsPadding = 70.dp

    // Backdrop card rotation angles
    const val backdropLeftRotation = -10f
    const val backdropRightRotation = 8f
    const val backdropAlpha = 0.7f
}
