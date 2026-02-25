package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotify add items to playlist request
 */
data class SpotifyAddItemsRequestDto(
    @SerializedName("uris")
    val uris: List<String>,

    @SerializedName("position")
    val position: Int? = null
)