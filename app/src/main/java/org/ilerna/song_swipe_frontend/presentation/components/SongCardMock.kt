package org.ilerna.song_swipe_frontend.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import org.ilerna.song_swipe_frontend.R
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeColors
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeDimens

/**
 * Mock version of the Song card used in the Swipe screen.
 *
 * Displays placeholder cover image, title and artist.
 * No real playback or network logic is handled in this component.
 */
@Composable
fun SongCardMock(
    song: SongUiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(SwipeDimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = SwipeColors.MainCard),
        elevation = CardDefaults.cardElevation(defaultElevation = SwipeDimens.SpacerL) // 16dp de “peso”
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SwipeDimens.CardInnerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.placeholder_album),
                contentDescription = "Cover",
                modifier = Modifier
                    .size(SwipeDimens.CoverSize)
                    .clip(RoundedCornerShape(SwipeDimens.CoverRadius))
            )

            Spacer(modifier = Modifier.height(SwipeDimens.SpacerM))

            Text(
                text = song.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(SwipeDimens.SpacerS))

            Text(
                text = song.artist,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(SwipeDimens.SpacerL))

            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(SwipeDimens.PlayButtonSize) // Play button is visual-only in this sprint

            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_media_play),
                        contentDescription = "Play",
                        tint = SwipeColors.MainCard
                    )
                }
            }
        }
    }
}
