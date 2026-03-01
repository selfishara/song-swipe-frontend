package org.ilerna.song_swipe_frontend.presentation.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.presentation.theme.Sizes
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme
import org.ilerna.song_swipe_frontend.presentation.theme.Spacing

/**
 * Drawer menu item data class.
 */
data class DrawerMenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

/**
 * Navigation drawer content for the app.
 * Shows user info header and menu items.
 *
 * Menu items:
 * - Open in Spotify
 * - Theme toggle
 * - Settings
 * - Sign Out
 *
 * @param user The current logged-in user
 * @param onOpenSpotify Callback to open Spotify app/web
 * @param onThemeClick Callback to toggle theme
 * @param onSettingsClick Callback to open settings (optional for future)
 * @param onSignOut Callback to sign out
 * @param modifier Modifier for the drawer
 */
@Composable
fun NavigationDrawerContent(
    user: User?,
    onOpenSpotify: () -> Unit,
    onThemeClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = Spacing.lg)
        ) {
            // Header with user info
            DrawerHeader(user = user)

            Spacer(modifier = Modifier.height(Spacing.lg))
            HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
            Spacer(modifier = Modifier.height(Spacing.md))

            // Menu items
            DrawerItem(
                icon = Icons.Default.OpenInNew,
                title = "Open in Spotify",
                onClick = onOpenSpotify
            )

            DrawerItem(
                icon = Icons.Default.Palette,
                title = "Theme",
                onClick = onThemeClick
            )

            DrawerItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = onSettingsClick
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
            Spacer(modifier = Modifier.height(Spacing.md))

            // Sign out at bottom
            DrawerItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Sign Out",
                onClick = onSignOut,
                isDestructive = true
            )
        }
    }
}

/**
 * Drawer header showing user avatar and info.
 */
@Composable
private fun DrawerHeader(
    user: User?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        horizontalAlignment = Alignment.Start
    ) {
        UserAvatar(
            user = user,
            size = Sizes.avatarLarge
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = user?.displayName ?: "Guest",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (user?.email != null) {
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Single drawer menu item.
 */
@Composable
private fun DrawerItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    NavigationDrawerItem(
        modifier = modifier.padding(horizontal = Spacing.sm),
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor
            )
        },
        label = {
            Text(
                text = title,
                color = contentColor
            )
        },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = MaterialTheme.colorScheme.surface,
            unselectedTextColor = contentColor,
            unselectedIconColor = contentColor
        )
    )
}

/* PREVIEWS */
@Preview(showBackground = true)
@Composable
private fun PreviewNavigationDrawer() {
    SongSwipeTheme {
        NavigationDrawerContent(
            user = User(
                id = "1",
                email = "john@example.com",
                displayName = "John Doe",
                profileImageUrl = null
            ),
            onOpenSpotify = {},
            onThemeClick = {},
            onSettingsClick = {},
            onSignOut = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNavigationDrawerDark() {
    SongSwipeTheme(darkTheme = true) {
        NavigationDrawerContent(
            user = User(
                id = "1",
                email = "john@example.com",
                displayName = "John Doe",
                profileImageUrl = null
            ),
            onOpenSpotify = {},
            onThemeClick = {},
            onSettingsClick = {},
            onSignOut = {}
        )
    }
}
