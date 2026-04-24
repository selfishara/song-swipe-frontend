package org.ilerna.song_swipe_frontend.presentation.components.layout

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.presentation.navigation.Screen
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing

/**
 * Dynamic top app bar for authenticated screens.
 * Shows user avatar and dynamic title based on current screen.
 *
 * Title logic:
 * - VibeScreen: (no title, avatar only)
 * - SwipeScreen: active playlist chip in top-right corner
 * - PlaylistsScreen: "My Playlists"
 *
 * @param user The current logged-in user
 * @param currentScreen The current screen for dynamic title
 * @param onAvatarClick Callback when avatar is clicked (opens drawer)
 * @param activePlaylistName Name of the active playlist (shown on SwipeScreen)
 * @param onActivePlaylistClick Callback when the active playlist chip is clicked
 * @param modifier Modifier for the top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    user: User?,
    currentScreen: Screen?,
    onAvatarClick: () -> Unit,
    activePlaylistName: String? = null,
    onActivePlaylistClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val title = when (currentScreen) {
        is Screen.Vibe -> null // Avatar only on vibe screen
        is Screen.Swipe -> null // No title on swipe screen
        is Screen.Playlists -> "My Playlists"
        else -> null
    }

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            // Empty - content is in navigationIcon
        },
        navigationIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(Spacing.md))

                UserAvatar(
                    user = user,
                    size = Sizes.avatarMedium,
                    onClick = onAvatarClick
                )

                if (title != null) {
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = {
            if (currentScreen is Screen.Swipe && onActivePlaylistClick != null) {
                AssistChip(
                    onClick = onActivePlaylistClick,
                    label = {
                        Text(
                            text = activePlaylistName ?: "Choose playlist",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.PlaylistAdd,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.padding(end = Spacing.md)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

/* PREVIEWS */
@Preview(showBackground = true)
@Composable
private fun PreviewTopAppBarVibe() {
    SongSwipeTheme {
        TopAppBar(
            user = User(
                id = "1",
                email = "john@example.com",
                displayName = "John Doe",
                profileImageUrl = null
            ),
            currentScreen = Screen.Vibe,
            onAvatarClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTopAppBarSwipe() {
    SongSwipeTheme {
        TopAppBar(
            user = User(
                id = "1",
                email = "john@example.com",
                displayName = "John Doe",
                profileImageUrl = null
            ),
            currentScreen = Screen.Swipe,
            onAvatarClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTopAppBarPlaylists() {
    SongSwipeTheme {
        TopAppBar(
            user = User(
                id = "1",
                email = "john@example.com",
                displayName = "John Doe",
                profileImageUrl = null
            ),
            currentScreen = Screen.Playlists,
            onAvatarClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTopAppBarDark() {
    SongSwipeTheme(darkTheme = true) {
        TopAppBar(
            user = User(
                id = "1",
                email = "john@example.com",
                displayName = "John Doe",
                profileImageUrl = null
            ),
            currentScreen = Screen.Vibe,
            onAvatarClick = {}
        )
    }
}
