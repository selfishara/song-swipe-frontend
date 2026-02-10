package org.ilerna.song_swipe_frontend.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeDirection

@Composable
fun SwipeButton(
    direction: SwipeDirection,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor = if (direction == SwipeDirection.LEFT) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val symbol = if (direction == SwipeDirection.LEFT) "✕" else "❤"

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = containerColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = containerColor.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = symbol,
            fontSize = 50.sp)
    }
}
