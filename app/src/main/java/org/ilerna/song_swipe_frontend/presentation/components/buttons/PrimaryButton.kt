package org.ilerna.song_swipe_frontend.presentation.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import org.ilerna.song_swipe_frontend.presentation.theme.Borders
import org.ilerna.song_swipe_frontend.presentation.theme.NeonGradient
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing

/**
 * Defines the available gradient styles for buttons.
 */
enum class ButtonStyle {
    /** Main neon gradient used as the default primary button style. */
    PRIMARY,
    /** Genre selection style — neutral when unselected, gradient when selected. */
    GENRE,
    /** Action / CTA style — always shows the gradient; only text dims when disabled. */
    ACTION
}

/**
 * Returns the gradient brush for the given button style.
 * Uses [Brush.horizontalGradient] to fill exactly the available width.
 */
@Composable
private fun ButtonStyle.brush(): Brush = when (this) {
    ButtonStyle.PRIMARY -> Brush.horizontalGradient(colors = NeonGradient)
    ButtonStyle.GENRE -> Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,  // Magenta
            MaterialTheme.colorScheme.tertiaryContainer  // Lavender
        )
    )
    ButtonStyle.ACTION -> Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.errorContainer,    // Peach
            MaterialTheme.colorScheme.primaryContainer   // Magenta
        )
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    shape: Shape = MaterialTheme.shapes.medium,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    // GENRE buttons use a neutral surface when unselected → gradient when selected.
    // ACTION / PRIMARY buttons always display the gradient.
    val useGradient = style != ButtonStyle.GENRE || isSelected

    // For ACTION buttons the container always stays fully visible so the gradient
    // never disappears; only the content dims to communicate the disabled state.
    val containerAlpha = if (style == ButtonStyle.ACTION || useGradient) 1f
                         else if (!enabled) 0.45f else 1f
    val contentAlpha   = if (!enabled) 0.45f else 1f

    // Content tint differs depending on whether the button has a gradient background.
    val contentColor = if (useGradient) Color.White
                       else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.buttonHeight)
            .clip(shape)
            .then(
                if (useGradient) {
                    Modifier.background(style.brush())
                } else {
                    // Unselected GENRE button — neutral surface + subtle outline
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            BorderStroke(Borders.thin, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            shape
                        )
                }
            )
            .alpha(containerAlpha)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.alpha(contentAlpha)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(Sizes.iconSmall)
                )
            }
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

