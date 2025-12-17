package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotify image object
 * Represents an image returned by the Spotify API
 */
data class SpotifyImageDto(
    @SerializedName("url")
    val url: String,

    @SerializedName("height")
    val height: Int?,

    @SerializedName("width")
    val width: Int?
)