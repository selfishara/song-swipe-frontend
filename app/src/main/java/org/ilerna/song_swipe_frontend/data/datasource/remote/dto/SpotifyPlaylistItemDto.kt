package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

data class SpotifyPlaylistItemDto(
    val track: SpotifyTrackDto?,
    @SerializedName("is_local")
    val isLocal: Boolean
)