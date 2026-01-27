package org.ilerna.song_swipe_frontend.domain.model

/**
 * Domain model representing a simplified Spotify playlist.
 * Used to expose clean playlist data to the application layer.
 */
data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val url: String?,
    val imageUrl: String?
)
