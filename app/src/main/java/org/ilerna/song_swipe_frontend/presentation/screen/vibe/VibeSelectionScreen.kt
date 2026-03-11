package org.ilerna.song_swipe_frontend.presentation.screen.vibe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.ilerna.song_swipe_frontend.presentation.components.buttons.ButtonStyle
import org.ilerna.song_swipe_frontend.presentation.components.buttons.PrimaryButton
import org.ilerna.song_swipe_frontend.presentation.theme.Radius
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme


/**
 * Genre entry pairing a display label with a representative Material icon.
 */
private data class GenreItem(val label: String, val icon: ImageVector)

/**
 * Static screen that lets the user choose a music vibe (genre).
 * Each genre is displayed as a button and redirects directly to the next screen.
 */
@Composable
fun VibeSelectionScreen(
    modifier: Modifier = Modifier, onContinueClick: (String) -> Unit = {}
) {
    // Dict of available genres displayed as buttons
    val genres = linkedMapOf(
        "33BFKADndu09rZdtTLl8Vw" to "Electronic",
        "37i9dQZF1DXcd2Vmhfon1w" to "Hip Hop",
        "37i9dQZF1DWTyTiBbDrmml" to "Pop",
        "37i9dQZF1DWTcqUzwhNmKv" to "Metal",
        "37i9dQZF1DXaxEKcoCdWHD" to "Reggaeton"
    )

    // Holds the selected genre (only 1 allowed)
    var selectedGenreId by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Enables vertical scrolling in case the content does not fit on screen
            .verticalScroll(rememberScrollState())
            // Standardized horizontal padding -- same as LoginScreen (Spacing.xl)
            .padding(horizontal = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(Spacing.xxxl))

        // Main title -- short and direct
        Text(
            text = "What's the Vibe?",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        //Spacer(Modifier.height(Spacing.xs))

        // Subtitle
        Text(
            text = "Start your music experience",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )

        Spacer(Modifier.height(Spacing.xxl))

        // One button per genre, stacked vertically (selected 1)
        genres.forEach { (id,name) -> val isSelected = selectedGenreId == id

            PrimaryButton(
                text = name.uppercase(),
                onClick = {
                    // Toggle selection (click again to unselect)
                    selectedGenreId = if (isSelected) null else id },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.pill)),
                style = ButtonStyle.GENRE,
                isSelected = isSelected,
                enabled = true
            )
            // Tighter spacing between genre buttons
            Spacer(Modifier.height(Spacing.md))
        }

        Spacer(Modifier.height(Spacing.lg))

        // Continue -- always shows gradient; content disabled until a genre is selected
        PrimaryButton(
            text = "CONTINUE",
            onClick = {
                selectedGenreId?.let { onContinueClick(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radius.pill),
            style = ButtonStyle.ACTION,
            enabled = selectedGenreId != null
        )

        // Bottom margin so the button clears the navigation bar
        Spacer(Modifier.height(Spacing.xxl))
    }
}

/**
 * Preview used only for design-time visualization.
 * Wrapped with SongSwipeTheme to apply correct colors and typography.
 */
@Preview(showBackground = true)
@Composable
private fun VibeSelectionScreenPreview() {
    SongSwipeTheme {
        VibeSelectionScreen()
    }
}