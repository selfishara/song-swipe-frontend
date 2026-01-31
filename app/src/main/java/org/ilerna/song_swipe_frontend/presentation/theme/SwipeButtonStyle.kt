package org.ilerna.song_swipe_frontend.presentation.theme

import CianIntenso
import GradienteNeon
import Lavanda
import Melocoton
import RosaNeonIntenso
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

/**
 * Defines the available gradient styles for buttons.
 * Naming follows the design intentions:
 * - PrimaryGradient: main neon gradient used across the app
 * - Genre: gradient used for genre selection buttons
 * - Action: gradient used for main call-to-action buttons (e.g. Continue)
 */
sealed class SwipeButtonStyle {

    /** Main neon gradient used as the default primary button style. */
    object PrimaryGradient : SwipeButtonStyle()

    /** Genre selection style.
     Matches the mockup gradient (RosaNeonIntenso -> Lavanda). */
    object Genre : SwipeButtonStyle()

    /** Action / CTA style.
    * Used for buttons like "Continue" (Melocoton -> RosaNeonIntenso). */
    object Action : SwipeButtonStyle()


    /**
     * Returns the gradient background depending on the selected style.
     */
    fun brush(): Brush = when (this) {
        PrimaryGradient -> Brush.linearGradient(
            colors = GradienteNeon,
            start = Offset(0f, 0f),
            end = Offset(1000f, 0f)
        )

        Genre -> Brush.linearGradient(
            colors = listOf(RosaNeonIntenso, Lavanda),
            start = Offset(0f, 0f),
            end = Offset(800f, 100f)
        )

        Action -> Brush.linearGradient(
            colors = listOf(Melocoton, RosaNeonIntenso),
            start = Offset(0f, 0f),
            end = Offset(1000f, 0f)
        )
    }

    /**
     * Accent color used for secondary buttons or selected states.
     */
    fun secondaryColor() = when (this) {
        PrimaryGradient -> CianIntenso
        Genre -> Lavanda
        Action -> RosaNeonIntenso
    }
}
