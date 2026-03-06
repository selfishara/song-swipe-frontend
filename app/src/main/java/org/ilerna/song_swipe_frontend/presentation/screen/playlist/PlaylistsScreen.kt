package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing
import org.ilerna.song_swipe_frontend.presentation.theme.NeonGradient

/**
 * PlaylistsScreen
 *
 * This screen displays the user's default "liked" playlist.
 * It handles:
 * - Loading state
 * - Error state with retry
 * - Empty state
 * - Success state with track list
 *
 * All business logic is handled inside PlaylistViewModel.
 */
@Composable
fun PlaylistsScreen(
    viewModel: PlaylistViewModel, supabaseUserId: String = "", spotifyUserId: String = "",

    modifier: Modifier = Modifier
) {

    // Collect only liked tracks state from ViewModel
    val state by viewModel.likedTracksState.collectAsState()

    /**
     * Load liked tracks when screen is opened
     * and user IDs are available.
     */
    LaunchedEffect(supabaseUserId, spotifyUserId) {
        if (supabaseUserId.isNotBlank() && spotifyUserId.isNotBlank()) {
            viewModel.loadLikedTracks(supabaseUserId, spotifyUserId)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        when (val s = state) {

            is UiState.Idle -> {
                CenterMessage(
                    title = "SONGS YOU HAVE LIKED",
                    message = "Open this screen after login to load your liked tracks."
                )
            }

            is UiState.Loading -> LoadingState()

            is UiState.Error -> ErrorState(
                message = s.message ?: "Unknown error",
                onRetry = { viewModel.retryLikedTracks(supabaseUserId, spotifyUserId) })

            is UiState.Success -> {
                val tracks = s.data
                if (tracks.isEmpty()) {
                    EmptyState()
                } else {
                    LikedTracksList(tracks = tracks)
                }
            }
        }
    }
}

/*  LIST  */

@Composable
private fun LikedTracksList(tracks: List<PlaylistTrackUi>) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {

        // Gradient section header
        item {
            Text(
                text = "SONGS YOU HAVE LIKED",
                style = MaterialTheme.typography.titleMedium.merge(
                    TextStyle(
                        brush = Brush.linearGradient(NeonGradient)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center

            )
            Spacer(Modifier.height(Spacing.md))
        }

        items(tracks, key = { it.id }) { track ->
            TrackCard(track = track)
        }
    }
}

/*  TRACK CARD  */

@Composable
private fun TrackCard(track: PlaylistTrackUi) {

    Card(
        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {


            AsyncImage(
                model = track.imageUrl,
                contentDescription = "Track image",
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(Icons.Outlined.MusicNote),
                error = rememberVectorPainter(Icons.Outlined.MusicNote),
            )

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artists,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { /* Remove feature will be implemented later */ }) {
                Icon(Icons.Outlined.Close, contentDescription = "Remove track")
            }
        }
    }
}

/*  STATES  */

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(Spacing.md))
        Text("Loading your liked songs…")
    }
}

@Composable
private fun EmptyState() {
    CenterMessage(
        title = "No liked songs yet",
        message = "Start swiping and your liked tracks will appear here."
    )
}

@Composable
private fun ErrorState(
    message: String, onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Couldn't load your liked songs")
        Spacer(Modifier.height(Spacing.sm))
        Text(message)
        Spacer(Modifier.height(Spacing.lg))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun CenterMessage(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.sm))
        Text(message)
    }
}

/*  PREVIEWS (3 MOCK SONGS)  */

@Preview(showBackground = true)
@Composable
private fun PreviewPlaylistsScreen() {

    val mockTracks = listOf(
        PlaylistTrackUi("1", "Blinding Lights", "The Weeknd", null),
        PlaylistTrackUi("2", "As It Was", "Harry Styles", null),
        PlaylistTrackUi("3", "Starboy", "The Weeknd, Daft Punk", null)
    )

    SongSwipeTheme {
        LikedTracksList(tracks = mockTracks)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPlaylistsScreenDark() {

    val mockTracks = listOf(
        PlaylistTrackUi("1", "Blinding Lights", "The Weeknd", null),
        PlaylistTrackUi("2", "As It Was", "Harry Styles", null),
        PlaylistTrackUi("3", "Starboy", "The Weeknd, Daft Punk", null)
    )

    SongSwipeTheme(darkTheme = true) {
        LikedTracksList(tracks = mockTracks)
    }
}