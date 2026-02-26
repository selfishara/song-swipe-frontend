package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.presentation.theme.NeonGradient


/**
 * Screen that displays all tracks inside the user's default playlist.
 *
 * This screen observes the UiState from the ViewModel and renders:
 * - Loading state (centered loader)
 * - Error state (message + retry button)
 * - Success state (list of tracks)
 * - Empty state (if playlist has no tracks)
 */
@Composable
fun PlaylistItemsScreen(
    supabaseUserId: String,
    spotifyUserId: String,
    modifier: Modifier = Modifier,
    viewModel: PlaylistItemsViewModel = viewModel()
) {
    // Collect the state exposed by the ViewModel
    val state by viewModel.state.collectAsState()

    // Trigger data loading when the screen is first composed
    // or when user IDs change
    LaunchedEffect(supabaseUserId, spotifyUserId) {
        viewModel.load(supabaseUserId, spotifyUserId)
    }

    when (val s = state) {

        // Show a centered loader while data is being fetched
        is UiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Show error message and allow user to retry
        is UiState.Error -> {
            ErrorState(
                message = s.message,
                onRetry = { viewModel.retry(supabaseUserId, spotifyUserId) },
                modifier = modifier
            )
        }

        // Show list of tracks if data was successfully loaded
        is UiState.Success -> {
            val items = s.data ?: emptyList()

            // If playlist has no tracks, show empty state
            if (items.isEmpty()) {
                EmptyState(modifier = modifier)
            } else {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    // Gradient section title
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    brush = Brush.horizontalGradient(NeonGradient)
                                )
                            ) {
                                append("SONGS YOU HAVE LIKED")
                            }
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Each track is displayed inside a Card
                        items(
                            items = items,
                            key = { it.id }
                        ) { track ->
                            TrackCard(
                                track = track,
                                onRemove = {
                                    // Remove action will be implemented later
                                    // viewModel.remove(track.id, supabaseUserId, spotifyUserId)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Idle state: nothing is rendered (initial state)
        is UiState.Idle -> {
            Box(modifier = modifier.fillMaxSize())
        }
    }
}

/**
 * Composable that represents a single track inside a Card.
 * It shows:
 * - Album cover
 * - Track name
 * - Artist(s)
 * - Remove button (X)
 */
@Composable
private fun TrackCard(
    track: PlaylistTrackUi,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Track cover (image or fallback icon)
            TrackCover(
                imageUrl = track.imageUrl,
                contentDescription = track.title
            )

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {

                // Track title
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                // Artist names
                Text(
                    text = track.artists,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Remove button (X)
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove track"
                )
            }
        }
    }
}

/**
 * Displays the album cover using Coil.
 * If the image URL is null or invalid,
 * a fallback icon is shown instead.
 */
@Composable
private fun TrackCover(
    imageUrl: String?,
    contentDescription: String
) {
    val size = 56.dp

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = "No cover"
            )
        }
    }
}

/**
 * Shown when the playlist has no tracks.
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Your playlist is empty.\nAdd songs to see them here.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Shown when an error occurs while loading data.
 * Provides a retry button.
 */
@Composable
private fun ErrorState(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = message?.takeIf { it.isNotBlank() } ?: "Error loading playlist.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaylistItemsScreenPreview() {

    val fakeTracks = listOf(
        PlaylistTrackUi(
            id = "1",
            title = "Blinding Lights",
            artists = "The Weeknd",
            imageUrl = null
        ),
        PlaylistTrackUi(
            id = "2",
            title = "As It Was",
            artists = "Harry Styles",
            imageUrl = null
        ),
        PlaylistTrackUi(
            id = "3",
            title = "Starboy",
            artists = "The Weeknd, Daft Punk",
            imageUrl = null
        )
    )

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                brush = Brush.horizontalGradient(NeonGradient)
                            )
                        ) {
                            append("SONGS YOU HAVE LIKED")
                        }
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(fakeTracks, key = { it.id }) { track ->
                        TrackCard(
                            track = track,
                            onRemove = {}
                        )
                    }
                }
            }
        }
    }
}