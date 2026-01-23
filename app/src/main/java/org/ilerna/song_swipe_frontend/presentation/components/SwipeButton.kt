package org.ilerna.song_swipe_frontend.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeDirection
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeColors

@Composable
fun SwipeButton(
    direction: SwipeDirection,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor = if (direction == SwipeDirection.LEFT) SwipeColors.Dislike else SwipeColors.Like
    val emoji = if (direction == SwipeDirection.LEFT) "❌" else "✅"

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = Modifier.size(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Text(text = emoji)
    }
}
