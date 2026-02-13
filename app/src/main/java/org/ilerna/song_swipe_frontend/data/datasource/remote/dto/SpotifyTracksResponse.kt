package org.ilerna.song_swipe_frontend.data.datasource.remote.dto
import com.google.gson.annotations.SerializedName

data class SpotifyTracksResponse(
    @SerializedName("items")
    val items: List<SpotifyPlaylistItemDto>,
    val next : String?,
    val offset: Int = 0,
    val limit: Int = 0,
    val total: Int = 0
)