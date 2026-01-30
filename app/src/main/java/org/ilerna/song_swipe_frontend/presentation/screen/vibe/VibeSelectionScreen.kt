package org.ilerna.song_swipe_frontend.presentation.screen.vibe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.presentation.components.PrimaryButton
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.SwipeButtonStyle


/**
Static screen that lets the user choose a music vibe (genre).
Each genre is displayed as a button and redirects directly to the next screen.
 */
@Composable
fun VibeSelectionScreen(
    modifier: Modifier = Modifier, onGenreClick: (String) -> Unit = {}
) {
    // List of available genres displayed as buttons
    val genres = listOf(
        "Electronic", "Hip Hop", "Pop", "Metal", "Reggaeton"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Enables vertical scrolling in case the content does not fit on screen
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))

        // Main title of the screen
        Text(
            text = "What's the vibe for your swipe today?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,

            )

        Spacer(Modifier.height(16.dp))

        // Subtitle shown in italic to visually separate it from the title
        Text(
            text = "Choose a maximum of 1 genres and\n we'll prepare your feed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )

        Spacer(Modifier.height(50.dp))

        // One button per genre, stacked vertically
        genres.forEach { genre ->
            PrimaryButton(
                text = genre.uppercase(),
                // When clicked, notifies the parent which genre was selected
                onClick = { onGenreClick(genre) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Sizes.borderCornerRadius)),
                // Uses the mockup button style instead of the neon one
                style = SwipeButtonStyle.Mockup
            )
            Spacer(Modifier.height(50.dp))
        }
    }
}

/**
Preview used only for design-time visualization.
Wrapped with SongSwipeTheme to apply correct colors and typography.
 */
@Preview(showBackground = true)
@Composable
private fun VibeSelectionScreenPreview() {
    SongSwipeTheme {
        VibeSelectionScreen()
    }
}