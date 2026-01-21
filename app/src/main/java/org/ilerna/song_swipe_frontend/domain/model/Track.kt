package org.ilerna.song_swipe_frontend.domain.model

data class Track(
    val album: Album,
    val artists: List<Artist>,
    val duration_ms: Int,
    val id: String,
    val is_playable: Boolean,
    val name: String,
    val preview_url: String,
    val type: String,
    val uri: String
)