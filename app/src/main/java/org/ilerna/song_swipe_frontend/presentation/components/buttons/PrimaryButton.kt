package org.ilerna.song_swipe_frontend.presentation.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.presentation.theme.NeonGradient
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes

/**
 * Defines the available gradient styles for buttons.
 */
enum class ButtonStyle {
    /** Main neon gradient used as the default primary button style. */
    PRIMARY,
    /** Genre selection style (primaryContainer → tertiary). */
    GENRE,
    /** Action / CTA style (errorContainer → primaryContainer). */
    ACTION
}

/**
 * Returns the gradient brush for the given button style.
 */
@Composable
private fun ButtonStyle.brush(): Brush = when (this) {
    ButtonStyle.PRIMARY -> Brush.linearGradient(
        colors = NeonGradient,
        start = Offset(0f, 0f),
        end = Offset(1000f, 0f)
    )
    ButtonStyle.GENRE -> Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,  // Magenta
            MaterialTheme.colorScheme.tertiaryContainer  // Lavender
        ),
        start = Offset(0f, 0f),
        end = Offset(800f, 100f)
    )
    ButtonStyle.ACTION -> Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.errorContainer,    // Peach
            MaterialTheme.colorScheme.primaryContainer   // Magenta
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 0f)
    )
}

/**
 * Returns the secondary/accent color for the given button style.
 */
@Composable
private fun ButtonStyle.secondaryColor(): Color = when (this) {
    ButtonStyle.PRIMARY -> MaterialTheme.colorScheme.secondary        // Cyan
    ButtonStyle.GENRE -> MaterialTheme.colorScheme.tertiaryContainer  // Lavender
    ButtonStyle.ACTION -> MaterialTheme.colorScheme.primaryContainer  // Magenta
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    isSelected: Boolean = false,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.45f
    val border = if (isSelected) {
        BorderStroke(2.dp, style.secondaryColor())
    } else null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.buttonHeight)
            .clip(MaterialTheme.shapes.medium)
            .background(style.brush())
            .then(
                if (border != null) Modifier.border(border, MaterialTheme.shapes.medium)
                else Modifier
            )
            .alpha(alpha)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}
