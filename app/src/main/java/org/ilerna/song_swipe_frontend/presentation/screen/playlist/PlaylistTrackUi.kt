package org.ilerna.song_swipe_frontend.presentation.screen.playlist

/**
 * UI Model (Null-safe)
 * Used by the playlist UI to display track info.
 */
data class PlaylistTrackUi(
    val id: String,
    val title: String,
    val artists: String,
    val imageUrl: String?
)