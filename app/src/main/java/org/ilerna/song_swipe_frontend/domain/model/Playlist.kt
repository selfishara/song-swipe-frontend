package org.ilerna.song_swipe_frontend.domain.model
/**
 * Data class representing a Playlist in the domain layer.
 */
data class Playlist(
    val id: String,
    val name: String,
    val description: String?,
    val isPublic: Boolean,
    val externalUrl: String
)