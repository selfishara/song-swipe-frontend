package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO that represents a simplified Spotify playlist.
 * Contains only the basic information needed for the application.
 */
data class SpotifySimplifiedPlaylistDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("public")
    val isPublic: Boolean = false,

    @SerializedName("external_urls")
    val externalUrls: SpotifyExternalUrlsDto?,

    @SerializedName("images")
    val images: List<SpotifyImageDto>?
)
