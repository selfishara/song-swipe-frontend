package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName
/**
 * DTO for Spotify playlist tracks response
 */
data class PlaylistTracksResponseDto(
    @SerializedName("items")
    val items: List<PlaylistTrackItemDto>,

    @SerializedName("total")
    val total: Int?,

    @SerializedName("limit")
    val limit: Int?,

    @SerializedName("offset")
    val offset: Int?
)

/**
 * DTO for individual track item in a playlist
 */
data class PlaylistTrackItemDto(
    @SerializedName("track")
    val track: SpotifyTrackDto,

    @SerializedName("added_at")
    val addedAt: String?
)