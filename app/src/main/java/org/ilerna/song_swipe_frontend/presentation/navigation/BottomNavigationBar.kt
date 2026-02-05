package org.ilerna.song_swipe_frontend.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.ilerna.song_swipe_frontend.presentation.theme.SongSwipeTheme

/**
 * Bottom navigation bar component for the main screens.
 * Uses NavController for type-safe navigation.
 *
 * Shows three tabs: Vibe, Swipe, and Playlists
 *
 * @param navController The NavController for navigation
 * @param modifier Modifier for the navigation bar
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavItems.forEach { item ->
            // For Swipe, we need to check if route starts with "swipe" due to optional params
            val isSelected = when (item) {
                is BottomNavItem.Swipe -> currentRoute?.startsWith("swipe") == true
                else -> currentRoute == item.route
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    // Avoid navigating to the same destination
                    val shouldNavigate = when (item) {
                        is BottomNavItem.Swipe -> currentRoute?.startsWith("swipe") != true
                        else -> currentRoute != item.route
                    }
                    
                    if (shouldNavigate) {
                        val route = when (item) {
                            is BottomNavItem.Swipe -> Screen.Swipe.createRoute()
                            else -> item.route
                        }
                        
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(Screen.Vibe.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBottomNavigationBar() {
    SongSwipeTheme {
        BottomNavigationBar(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBottomNavigationBarDark() {
    SongSwipeTheme(darkTheme = true) {
        BottomNavigationBar(
            navController = rememberNavController()
        )
    }
}
