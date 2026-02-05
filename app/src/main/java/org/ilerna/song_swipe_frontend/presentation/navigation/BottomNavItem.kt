package org.ilerna.song_swipe_frontend.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.SwipeRight
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing bottom navigation items.
 * Each item has a screen, title, and icons for selected/unselected states.
 * Uses Screen routes for type-safe navigation.
 *
 * @property screen The associated Screen destination
 * @property title Display title for the navigation item
 * @property selectedIcon Icon when item is selected
 * @property unselectedIcon Icon when item is not selected
 */
sealed class BottomNavItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    /** Route string from the associated Screen */
    val route: String get() = screen.route

    /**
     * Vibe tab - Home screen with category selection
     */
    data object Vibe : BottomNavItem(
        screen = Screen.Vibe,
        title = "Vibe",
        selectedIcon = Icons.Filled.MusicNote,
        unselectedIcon = Icons.Outlined.MusicNote
    )

    /**
     * Swipe tab - Swipe session screen
     */
    data object Swipe : BottomNavItem(
        screen = Screen.Swipe,
        title = "Swipe",
        selectedIcon = Icons.Filled.SwipeRight,
        unselectedIcon = Icons.Outlined.SwipeRight
    )

    /**
     * Playlists tab - User's saved playlists
     */
    data object Playlists : BottomNavItem(
        screen = Screen.Playlists,
        title = "Playlists",
        selectedIcon = Icons.AutoMirrored.Filled.PlaylistPlay,
        unselectedIcon = Icons.AutoMirrored.Outlined.PlaylistPlay
    )
}

/**
 * List of all bottom navigation items for easy iteration.
 */
val bottomNavItems = listOf(
    BottomNavItem.Vibe,
    BottomNavItem.Swipe,
    BottomNavItem.Playlists
)
