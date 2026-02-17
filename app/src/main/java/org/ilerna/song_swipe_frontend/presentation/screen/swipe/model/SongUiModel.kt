package org.ilerna.song_swipe_frontend.presentation.screen.swipe.model

/**
 * UI model representing a song in the Swipe screen.
 *
 * This model is designed to be simple and focused on the data needed for display
 * and interaction in the Swipe UI. It abstracts away any domain or data layer complexities.
 *
 * @property id Unique identifier for the song (e.g., Spotify track ID)
 * @property title The name of the song
 * @property artist The primary artist's name
 * @property imageUrl URL for the song's album art (optional)
 * @property previewUrl URL for a 30-second preview of the track (optional)
 */
data class SongUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String? = null,
    val previewUrl: String? = null
)
