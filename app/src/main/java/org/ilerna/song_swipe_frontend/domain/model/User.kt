package org.ilerna.song_swipe_frontend.domain.model

/**
 * Domain model representing a user in the application
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val spotifyId: String? = null
)
