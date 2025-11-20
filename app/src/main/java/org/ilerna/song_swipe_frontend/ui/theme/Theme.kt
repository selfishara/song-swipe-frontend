package org.ilerna.song_swipe_frontend.ui.theme

import CianIntenso
import GrisProfundo
import Lavanda
import Melocoton
import RosaNeonIntenso
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.*

// SongSwipe Main Color Palette (Dark theme)
private val DarkColorScheme = darkColorScheme(
    primary = RosaNeonIntenso,
    onPrimary = Color.Black,
    secondary = CianIntenso,
    tertiary = Lavanda,
    background = GrisProfundo,
    surface = GrisProfundo,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Melocoton
)

// SongSwipe Main Color Palette (Only Dark Mode)
@Composable
fun SongSwipeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme, typography = Typography, shapes = shapes, content = content
    )
}
