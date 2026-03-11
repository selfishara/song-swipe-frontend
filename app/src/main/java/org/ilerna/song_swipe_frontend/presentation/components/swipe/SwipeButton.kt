package org.ilerna.song_swipe_frontend.presentation.components.swipe

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.SwipeDirection
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeDislike
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeLike

@Composable
fun SwipeButton(
    direction: SwipeDirection,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor = if (direction == SwipeDirection.LEFT) SwipeDislike else SwipeLike
    val icon = if (direction == SwipeDirection.LEFT) Icons.Rounded.Close else Icons.Rounded.Favorite
    val contentDescription = if (direction == SwipeDirection.LEFT) "Dislike" else "Like"

    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(Sizes.buttonCircle),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.4f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(Sizes.iconLarge)
        )
    }
}
