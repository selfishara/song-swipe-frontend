package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.components.CreatePlaylistDialog
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing

/**
 * PlaylistsScreen
 *
 * Displays every Spotify playlist the user owns or follows. The user picks
 * one as the "active" playlist — the destination for tracks liked while swiping.
 * Tapping a playlist navigates into its details (track list).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    viewModel: PlaylistListViewModel,
    onPlaylistClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.playlistsState.collectAsState()
    val activePlaylistId by viewModel.activePlaylistId.collectAsState()
    val createError by viewModel.createError.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (state is UiState.Idle) viewModel.loadPlaylists()
    }

    val isRefreshing = state is UiState.Loading
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Outlined.Add, contentDescription = "Create playlist")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadPlaylists() }
        ) {
            when (val s = state) {
                is UiState.Idle, is UiState.Loading -> LoadingState()

                is UiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = { viewModel.loadPlaylists() }
                )

                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        EmptyState()
                    } else {
                        PlaylistList(
                            playlists = s.data,
                            activePlaylistId = activePlaylistId,
                            onPlaylistClick = onPlaylistClick,
                            onSetActive = { viewModel.setActivePlaylist(it) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = {
                showCreateDialog = false
                viewModel.clearCreateError()
            },
            onConfirm = { name, description, isPublic ->
                viewModel.createPlaylist(name, description, isPublic)
                showCreateDialog = false
            }
        )
    }

    createError?.let { message ->
        // Surface-side error hint; dialog already dismissed by this point
        LaunchedEffect(message) { /* no-op, could trigger snackbar in future */ }
    }
}

@Composable
private fun PlaylistList(
    playlists: List<Playlist>,
    activePlaylistId: String?,
    onPlaylistClick: (String) -> Unit,
    onSetActive: (Playlist) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        items(items = playlists, key = { it.id }) { playlist ->
            PlaylistCard(
                playlist = playlist,
                isActive = playlist.id == activePlaylistId,
                onClick = { onPlaylistClick(playlist.id) },
                onSetActive = { onSetActive(playlist) }
            )
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    isActive: Boolean,
    onClick: () -> Unit,
    onSetActive: () -> Unit
) {
    val borderModifier = if (isActive) {
        Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.large
        )
    } else Modifier

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = playlist.imageUrl,
                contentDescription = "Playlist cover",
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(Icons.Outlined.MusicNote),
                error = rememberVectorPainter(Icons.Outlined.MusicNote)
            )

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!playlist.description.isNullOrBlank()) {
                    Text(
                        text = playlist.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(Spacing.sm))

            if (isActive) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Active playlist",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(onClick = onSetActive) { Text("Set active") }
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
        Text("Loading your playlists…")
    }
}

@Composable
private fun EmptyState() {
    CenterMessage(
        title = "No playlists yet",
        message = "Create one to start saving songs as you swipe."
    )
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
        Text("Couldn't load your playlists")
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
