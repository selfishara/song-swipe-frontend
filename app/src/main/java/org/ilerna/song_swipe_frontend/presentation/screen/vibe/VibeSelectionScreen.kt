package org.ilerna.song_swipe_frontend.presentation.screen.vibe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.ilerna.song_swipe_frontend.presentation.components.buttons.ButtonStyle
import org.ilerna.song_swipe_frontend.presentation.components.buttons.PrimaryButton
import org.ilerna.song_swipe_frontend.presentation.theme.Radius
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing
import org.ilerna.song_swipe_frontend.presentation.theme.VibeGenreGradients

/**
 * Represents a genre item displayed as a visual card.
 * Includes label, subtitle (vibe description), icon and gradient background.
 */
private data class GenreItem(
    val label: String, val subtitle: String, val icon: ImageVector, val gradientColors: List<Color>
)

/**
 * Screen that allows the user to select a music vibe using a grid of visual cards.
 * Replaces the previous vertical button list with a more engaging UI.
 */
@Composable
fun VibeSelectionScreen(
    modifier: Modifier = Modifier,
    activeGenre: String? = null,
    onContinueClick: (String) -> Unit = {}
) {

    // Static genre list for MVP (can be replaced with backend data later)
    val genres = listOf(
        GenreItem(
            label = "Electronic",
            subtitle = "Synths & vibrations",
            icon = Icons.Filled.GraphicEq,
            gradientColors = VibeGenreGradients.Electronic
        ), GenreItem(
            label = "Hip Hop",
            subtitle = "Beats, flow & bars",
            icon = Icons.Filled.Mic,
            gradientColors = VibeGenreGradients.HipHop
        ), GenreItem(
            label = "Pop",
            subtitle = "Catchy & mainstream",
            icon = Icons.Filled.Star,
            gradientColors = VibeGenreGradients.Pop
        ), GenreItem(
            label = "Metal",
            subtitle = "Loud & powerful",
            icon = Icons.Filled.Bolt,
            gradientColors = VibeGenreGradients.Metal
        ), GenreItem(
            label = "Reggaeton",
            subtitle = "Rhythm & perreo",
            icon = Icons.Filled.MusicNote,
            gradientColors = VibeGenreGradients.Reggaeton
        ), GenreItem(
            label = "Indie",
            subtitle = "Alternative vibes",
            icon = Icons.Filled.Album,
            gradientColors = VibeGenreGradients.Indie
        )
    )

    // Holds the selected genre (single selection)
    var selectedGenre by rememberSaveable { mutableStateOf<String?>(activeGenre) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.xl), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(Spacing.xl))

        // Main title
        Text(
            text = "What's the Vibe?",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        // Subtitle
        Text(
            text = "Start your music experience",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic
        )

        Spacer(Modifier.height(Spacing.xxl))

        // Grid of genre cards (2 columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(
                top = 8.dp, bottom = Spacing.lg
            )
        ) {
            itemsIndexed(genres) { index, genre ->
                val isSelected = selectedGenre == genre.label

                GenreCard(
                    genre = genre, isSelected = isSelected, index = index, onClick = {
                        // Toggle selection
                        selectedGenre = if (isSelected) null else genre.label
                    })
            }
        }
        Spacer(Modifier.height(Spacing.md))

        // Continue button (enabled only when a genre is selected)
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

        Spacer(Modifier.height(Spacing.lg))
    }
}

/**
 * Individual genre card component.
 * Includes gradient background, icon, title, subtitle and selection state.
 */
@Composable
private fun GenreCard(
    genre: GenreItem, isSelected: Boolean, index: Int, onClick: () -> Unit
) {

    // Smooth scale animation when selected
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f, animationSpec = tween(
            durationMillis = 180, easing = FastOutSlowInEasing
        ), label = ""
    )
    var pressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f, animationSpec = tween(100), label = "pressScale"
    )

    val gradient = Brush.linearGradient(genre.gradientColors)

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(140L * index)
        visible = true
    }
    AnimatedVisibility(
        visible = visible, enter = fadeIn(
            animationSpec = tween(durationMillis = 450)
        ) + scaleIn(
            initialScale = 0.85f, animationSpec = tween(
                durationMillis = 450, easing = FastOutSlowInEasing
            )
        )
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .scale(scale * pressScale)
                .clip(RoundedCornerShape(24.dp))
                .background(gradient)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    }, onTap = {
                        onClick()
                    })
                }) {

            // Decorative abstract shapes (lightweight, no images required)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 28.dp, y = (-28).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp, y = 40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            // Genre icon
            Icon(
                imageVector = genre.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopStart)
            )

            // Text content (title + subtitle)
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = genre.label.uppercase(),
                    modifier = Modifier.basicMarquee(),
                    maxLines = 1,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black
                    )
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = genre.subtitle,
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }


            // Selection state (overlay + check icon)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.White), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VibeSelectionScreenPreview() {
    SongSwipeTheme {
        VibeSelectionScreen()
    }
}