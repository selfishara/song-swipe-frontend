package org.ilerna.song_swipe_frontend.domain.model

data class Album(
    val album_type: String,
    val artists: List<Artist>,
    val available_markets: List<String>,
    val href: String,
    val id: String,
    val images: List<Image>,
    val name: String,
    val release_date: String,
    val release_date_precision: String,
    val total_tracks: Int,
    val type: String,
    val uri: String
)