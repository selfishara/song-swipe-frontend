package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotify artist object
 */
data class SpotifyArtistDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String
)