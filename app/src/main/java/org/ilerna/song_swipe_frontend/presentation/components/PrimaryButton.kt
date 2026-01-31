package org.ilerna.song_swipe_frontend.presentation.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.presentation.theme.Inter
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeButtonStyle

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SwipeButtonStyle = SwipeButtonStyle.PrimaryGradient,
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
            fontSize = Sizes.buttonTextSize,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold
        )
    }
}
