package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for the paginated response from GET /v1/me/playlists.
 * Contains the current user's playlists with standard Spotify paging fields.
 */
data class SpotifyUserPlaylistsResponseDto(
    @SerializedName("items")
    val items: List<SpotifySimplifiedPlaylistDto>,

    @SerializedName("next")
    val next: String?,

    @SerializedName("offset")
    val offset: Int = 0,

    @SerializedName("limit")
    val limit: Int = 0,

    @SerializedName("total")
    val total: Int = 0
)
