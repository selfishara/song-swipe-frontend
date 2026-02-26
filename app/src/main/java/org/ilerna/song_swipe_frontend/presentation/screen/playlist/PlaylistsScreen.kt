package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import android.util.Log
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing

/**
 * Playlists Screen - Shows user's saved playlists and liked tracks.
 *
 * This is a placeholder screen for Phase 1.
 * Future implementation will show:
 * - List of user-created playlists
 * - Liked/saved tracks from swipe sessions
 *
 * @param getOrCreateDefaultPlaylistUseCase Use case to ensure default playlist exists
 * @param supabaseUserId The Supabase auth user ID
 * @param spotifyUserId The Spotify user ID
 * @param modifier Modifier for the screen
 */
@Composable
fun PlaylistsScreen(
    getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase? = null,
    supabaseUserId: String = "",
    spotifyUserId: String = "",
    modifier: Modifier = Modifier
) {
    // Ensure default playlist exists when this screen is opened
    if (getOrCreateDefaultPlaylistUseCase != null && supabaseUserId.isNotEmpty() && spotifyUserId.isNotEmpty()) {
        LaunchedEffect(Unit) {
            try {
                when (val result = getOrCreateDefaultPlaylistUseCase(supabaseUserId, spotifyUserId)) {
                    is NetworkResult.Success -> {
                        Log.d("PlaylistsScreen", "Default playlist ready: ${result.data.name}")
                    }
                    is NetworkResult.Error -> {
                        Log.e("PlaylistsScreen", "Error ensuring default playlist: ${result.message}")
                    }
                    is NetworkResult.Loading -> { /* no-op */ }
                }
            } catch (e: Exception) {
                Log.e("PlaylistsScreen", "Error ensuring default playlist: ${e.message}", e)
            }
        }
    }

    // TODO: Implement with PlaylistsViewModel when backend is ready
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.PlaylistPlay,
                contentDescription = "Playlists",
                modifier = Modifier.size(Sizes.iconXLarge),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "Your Playlists",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = "Your liked songs and playlists\nwill appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPlaylistsScreen() {
    SongSwipeTheme {
        PlaylistsScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPlaylistsScreenDark() {
    SongSwipeTheme(darkTheme = true) {
        PlaylistsScreen()
    }
}
