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
    modifier: Modifier = Modifier,
    activeGenre: String? = null,
    onContinueClick: (String) -> Unit = {}
) {
    
    // TODO: Migrate to dynamic genres/data from backend once available.
    // for now, this is a static list for design MVP purposes.
    // Available genres with representative icons
    val genres = listOf(
        GenreItem("Electronic", Icons.Filled.GraphicEq),
        GenreItem("Hip Hop",    Icons.Filled.Mic),
        GenreItem("Pop",        Icons.Filled.Star),
        GenreItem("Metal",      Icons.Filled.Bolt),
        GenreItem("Reggaeton",  Icons.Filled.MusicNote)
    )

    // Holds the selected genre (only 1 allowed). Pre-select the active genre if resuming.
    var selectedGenre by rememberSaveable { mutableStateOf<String?>(activeGenre) }

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

        // One button per genre, stacked vertically (max 1 selected)
        genres.forEach { genre ->
            val isSelected = selectedGenre == genre.label

            PrimaryButton(
                text = genre.label.uppercase(),
                onClick = {
                    // Toggle selection (tap again to deselect)
                    selectedGenre = if (isSelected) null else genre.label
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.pill),
                style = ButtonStyle.GENRE,
                isSelected = isSelected,
                enabled = true,
                leadingIcon = genre.icon
            )
            // Tighter spacing between genre buttons
            Spacer(Modifier.height(Spacing.md))
        }

        Spacer(Modifier.height(Spacing.lg))

        // Continue -- always shows gradient; content disabled until a genre is selected
        PrimaryButton(
            text = "CONTINUE",
            onClick = {
                selectedGenre?.let { onContinueClick(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radius.pill),
            style = ButtonStyle.ACTION,
            enabled = selectedGenre != null
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