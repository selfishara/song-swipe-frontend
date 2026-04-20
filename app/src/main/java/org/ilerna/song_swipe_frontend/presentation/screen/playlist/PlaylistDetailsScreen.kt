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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing

/**
 * Shows the tracks of a specific playlist. Users can remove tracks via the delete icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(
    viewModel: PlaylistViewModel,
    playlistId: String,
    modifier: Modifier = Modifier
) {
    val state by viewModel.tracksState.collectAsState()
    val trackToDelete by viewModel.trackToDelete.collectAsState()

    LaunchedEffect(playlistId) {
        if (playlistId.isNotBlank()) viewModel.loadTracks(playlistId)
    }

    val isRefreshing = state is UiState.Loading
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.retry(playlistId) }
    ) {
        Box(Modifier.fillMaxSize()) {
            when (val s = state) {
                is UiState.Idle, is UiState.Loading -> LoadingState()

                is UiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = { viewModel.retry(playlistId) }
                )

                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        EmptyState()
                    } else {
                        TrackList(
                            tracks = s.data,
                            onRemoveTrack = { viewModel.requestDeleteTrack(it) }
                        )
                    }
                }
            }
        }
    }

    trackToDelete?.let { track ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteTrack() },
            title = { Text("Remove track") },
            text = { Text("Remove \"${track.title}\" from this playlist?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteTrack(playlistId) }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteTrack() }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TrackList(
    tracks: List<PlaylistTrackUi>,
    onRemoveTrack: (PlaylistTrackUi) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        itemsIndexed(
            items = tracks,
            key = { index, track -> "${track.id}-$index" }
        ) { _, track ->
            TrackCard(track = track, onRemove = { onRemoveTrack(track) })
        }
    }
}

@Composable
private fun TrackCard(track: PlaylistTrackUi, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
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
                error = rememberVectorPainter(Icons.Outlined.MusicNote)
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

            IconButton(onClick = onRemove) {
                Icon(Icons.Outlined.Close, contentDescription = "Remove track")
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(Spacing.md))
        Text("Loading tracks…")
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("This playlist is empty", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.sm))
        Text("Swipe right on songs to add them here.")
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Couldn't load tracks")
        Spacer(Modifier.height(Spacing.sm))
        Text(message)
        Spacer(Modifier.height(Spacing.lg))
        Button(onClick = onRetry) { Text("Retry") }
    }
}
