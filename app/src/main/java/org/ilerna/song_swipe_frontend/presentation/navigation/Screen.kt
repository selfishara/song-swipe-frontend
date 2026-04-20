package org.ilerna.song_swipe_frontend.presentation.navigation

/**
 * Screen - Sealed class representing all navigation routes in the app
 *
 * This sealed class defines all available screens and their route strings
 * for navigation using Jetpack Compose Navigation.
 *
 * Screens:
 * - Login: Authentication screen
 * - Vibe: Home screen with category selection (was Home)
 * - Swipe: Session screen for swiping tracks (with optional playlistId)
 * - Playlists: User's list of Spotify playlists
 * - PlaylistDetails: Track list for a specific playlist
 *
 * Usage:
 * ```kotlin
 * navController.navigate(Screen.Login.route)
 * navController.navigate(Screen.Swipe.createRoute("playlistId123"))
 * navController.navigate(Screen.PlaylistDetails.createRoute("playlistId123"))
 * ```
 *
 * @property route The string route used for navigation
 */
sealed class Screen(val route: String) {

    /**
     * Login screen - User authentication with Spotify OAuth
     */
    data object Login : Screen("login")

    /**
     * Vibe screen - Home screen with category/genre selection
     * This is the start destination after login
     */
    data object Vibe : Screen("vibe")

    /**
     * Swipe screen - Session screen for swiping tracks
     * Accepts an optional playlistId parameter
     */
    data object Swipe : Screen("swipe?playlistId={playlistId}") {
        /**
         * Creates a route with an optional playlist ID parameter.
         * @param playlistId The Spotify playlist ID to load, or null for default/featured playlist
         * @return The route string with the playlist ID parameter
         */
        fun createRoute(playlistId: String? = null): String {
            return if (playlistId != null) {
                "swipe?playlistId=$playlistId"
            } else {
                "swipe"
            }
        }

        /** Route pattern for navigation argument extraction */
        const val ROUTE_PATTERN = "swipe?playlistId={playlistId}"

        /** Argument key for the playlist ID */
        const val ARG_PLAYLIST_ID = "playlistId"
    }

    /**
     * Playlists screen - Displays all user's Spotify playlists as a list
     */
    data object Playlists : Screen("playlists")

    /**
     * Playlist details screen - Shows tracks of a specific playlist.
     */
    data object PlaylistDetails : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: String): String = "playlist/$playlistId"

        const val ROUTE_PATTERN = "playlist/{playlistId}"
        const val ARG_PLAYLIST_ID = "playlistId"
    }

    companion object {
        /**
         * List of screens that show the bottom navigation bar.
         */
        val bottomNavScreens = listOf(Vibe, Swipe, Playlists)

        /**
         * Returns a list of all available screens
         */
        fun getAllScreens(): List<Screen> = listOf(
            Login,
            Vibe,
            Swipe,
            Playlists,
            PlaylistDetails
        )

        /**
         * Get Screen from route string.
         * @param route The route string to match
         * @return The matching Screen or null if not found
         */
        fun fromRoute(route: String?): Screen? {
            return when {
                route == null -> null
                route == Login.route -> Login
                route == Vibe.route -> Vibe
                route == Playlists.route -> Playlists
                route.startsWith("playlist/") -> PlaylistDetails
                route.startsWith("swipe") -> Swipe
                else -> null
            }
        }
    }
}
