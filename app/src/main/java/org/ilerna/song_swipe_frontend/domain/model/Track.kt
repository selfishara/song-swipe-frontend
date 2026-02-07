package org.ilerna.song_swipe_frontend.domain.model

data class Track(
    val id: String,
    val name: String,
    val album: AlbumSimplified,
    val artists: List<Artist>,
    val durationMs: Int,
    val isPlayable: Boolean,
    val previewUrl: String?,
    val type: String,
    val uri: String,
    val imageUrl: String?
)