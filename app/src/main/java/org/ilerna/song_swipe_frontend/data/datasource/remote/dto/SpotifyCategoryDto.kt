package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO that represents a Spotify category.
 * Categories are used as genres when fetching playlists.
 */
data class SpotifyCategoryDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String
)