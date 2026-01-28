package org.ilerna.song_swipe_frontend.presentation.screen.swipe.model

data class SongUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String? = null
)
