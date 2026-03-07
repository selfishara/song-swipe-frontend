package org.ilerna.song_swipe_frontend.presentation.components.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.R
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing

/**
 * SpotifyLoginButton
 *
 * A thin wrapper around [PrimaryButton] used exclusively for the Login screen.
 *
 * Why this wrapper exists:
 * - We keep [PrimaryButton] untouched to avoid affecting other screens.
 * - The Login CTA requires a leading Spotify icon with specific alignment.
 * - This preserves the design system (gradient, shape, height) while allowing
 *   screen-specific content composition.
 *
 * Implementation detail:
 * - We render [PrimaryButton] as the background layer.
 * - We draw a centered Row (icon + text) on top of it.
 * - We intentionally pass an empty text to [PrimaryButton] to avoid duplicated labels.
 */
@Composable
fun SpotifyLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Continue with Spotify"
) {
    // Both layers share the same size so the overlay aligns perfectly.
    val baseModifier = modifier
        .fillMaxWidth()
        .height(Sizes.buttonHeight)

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        // Background layer: keeps the design system (gradient, shape, click behavior).
        PrimaryButton(
            text = "",
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(Sizes.buttonHeight),
            style = ButtonStyle.PRIMARY
        )

        // Foreground layer: custom content (Spotify icon + text).
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_spotify),
                contentDescription = "Spotify",
                // PrimaryButton text is white; use the same for visual consistency.
                tint = Color.Unspecified, // IMPORTANT: do not tint the official logo
                modifier = Modifier
                    .size(18.dp)
                    // Small vertical tweak to match the mock.
                    .offset(y = (-1).dp)
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

/* PREVIEW */
@Preview(showBackground = true)
@Composable
private fun PreviewSpotifyLoginButton() {
    SongSwipeTheme {
        SpotifyLoginButton(onClick = {})
    }
}