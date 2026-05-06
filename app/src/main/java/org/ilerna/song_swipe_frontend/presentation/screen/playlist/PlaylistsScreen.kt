package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.presentation.components.animation.AnimatedGradientBorder
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.components.CreatePlaylistDialog
import org.ilerna.song_swipe_frontend.presentation.theme.Borders
import org.ilerna.song_swipe_frontend.presentation.theme.Radius
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
        containerColor = MaterialTheme.colorScheme.background
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
                            onSetActive = { viewModel.setActivePlaylist(it) },
                            onCreatePlaylist = { showCreateDialog = true }
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
    onSetActive: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.lg,
                bottom = 120.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
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

        Button(
            onClick = onCreatePlaylist,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    bottom = 3.dp
                )
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(Modifier.width(Spacing.sm))
            Text("Create new playlist")
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistCard(
    playlist: Playlist,
    isActive: Boolean,
    onClick: () -> Unit,
    onSetActive: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .shadow(
                elevation = 10.dp,
                shape = MaterialTheme.shapes.extraLarge,
                clip = false
            )
    ) {
        AnimatedGradientBorder(
            modifier = Modifier
                .matchParentSize()
                .padding(Borders.thin),
            strokeWidth = Borders.medium,
            cornerRadius = Radius.pill
        )

        Card(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable { onClick() },
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 1.00f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.md)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = playlist.imageUrl,
                                contentDescription = "Playlist cover",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(MaterialTheme.shapes.large),
                                contentScale = ContentScale.Crop,
                                placeholder = rememberVectorPainter(Icons.Outlined.MusicNote),
                                error = rememberVectorPainter(Icons.Outlined.MusicNote)
                            )
                        }

                        Spacer(Modifier.height(Spacing.md))

                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Visible,
                            modifier = Modifier.basicMarquee(
                                iterations = Int.MAX_VALUE,
                                repeatDelayMillis = 0,
                                initialDelayMillis = 0
                            )
                        )

                        if (!playlist.description.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = playlist.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                modifier = Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    repeatDelayMillis = 0,
                                    initialDelayMillis = 0
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onSetActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        enabled = !isActive
                    ) {
                        if (isActive) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text("Active")
                        } else {
                            Text("Set active")
                        }
                    }
                }
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
