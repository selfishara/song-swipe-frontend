package org.ilerna.song_swipe_frontend.presentation.navigation

/**
 * Screen - Sealed class representing all navigation routes in the app
 * 
 * This sealed class defines all available screens and their route strings
 * for navigation using Jetpack Compose Navigation.
 * 
 * Current implementation includes:
 * - Login: Authentication screen
 * 
 * Future screens prepared for bottom navigation:
 * - Home: Main home screen with featured content
 * - Playlists: User's playlists and saved content
 * - Settings: App settings and user preferences
 * 
 * Usage:
 * ```kotlin
 * navController.navigate(Screen.Login.route)
 * ```
 * 
 * @property route The string route used for navigation
 */
sealed class Screen(val route: String) {
    
    /**
     * Login screen - User authentication with Spotify OAuth
     */
    data object Login : Screen("login")
    
    // Future bottom navigation screens
    // Uncomment when implementing bottom navigation
    
    /**
     * Home screen - Main screen with featured content
     */
    data object Home : Screen("home")
    
    /**
     * Playlists screen - Display user's playlists and saved content
     */
    data object Playlists : Screen("playlists")
    
    /**
     * Settings screen - App settings, theme selection, and user preferences
     */
    data object Settings : Screen("settings")
    
    companion object {
        /**
         * Returns a list of all available screens
         */
        fun getAllScreens(): List<Screen> = listOf(
            Login,
            Home,
            Playlists,
            Settings
        )
        
        /**
         * Returns the list of bottom navigation screens
         * These screens will be accessible from the bottom navigation bar
         */
        fun getBottomNavigationScreens(): List<Screen> = listOf(
            Home,
            Playlists,
            Settings
        )
        
        /**
         * Finds a screen by its route
         * @param route The route string to search for
         * @return The Screen object or null if not found
         */
        fun fromRoute(route: String?): Screen? = when (route) {
            Login.route -> Login
            Home.route -> Home
            Playlists.route -> Playlists
            Settings.route -> Settings
            else -> null
        }
    }
}
