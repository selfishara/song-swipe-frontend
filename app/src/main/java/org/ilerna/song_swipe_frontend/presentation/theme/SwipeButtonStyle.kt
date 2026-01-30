package org.ilerna.song_swipe_frontend.presentation.theme

import CianIntenso
import GradienteNeon
import Lavanda
import RosaNeonIntenso
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

/**
Defines the available visual styles for primary and secondary buttons.
Each style encapsulates its own gradient and accent colors,
so buttons can switch appearance without duplicating logic.
 */
sealed class SwipeButtonStyle {

    /** Neon style used across most of the app.
    Uses the full neon gradient defined in the color palette.*/
    object Neon : SwipeButtonStyle()

    /** Mockup style used for screens that must match the design mockups.
    Uses a softer gradient from RosaNeonIntenso to Lavanda.*/
    object Mockup : SwipeButtonStyle()

    /**
    Returns the background gradient for the primary button
    depending on the selected button style.
     */
    fun brush(): Brush = when (this) {
        Neon -> Brush.linearGradient(
            colors = GradienteNeon, start = Offset(0f, 0f), end = Offset(1000f, 0f)
        )

        Mockup -> Brush.linearGradient(
            colors = listOf(RosaNeonIntenso, Lavanda),
            start = Offset(0f, 0f),
            end = Offset(800f, 100f)
        )
    }

    /**
    Returns the accent color used by secondary buttons
    (border and text color), depending on the style.
     */
    fun secondaryColor() = when (this) {
        Neon -> CianIntenso
        Mockup -> Lavanda
    }
}
