package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotify album object
 */
data class SpotifyAlbumDto(
    @SerializedName("album_type")
    val albumType: String?,

    @SerializedName("artists")
    val artists: List<SpotifyArtistDto>?,

    @SerializedName("available_markets")
    val availableMarkets: List<String>?,

    @SerializedName("href")
    val href: String?,

    @SerializedName("id")
    val id: String?,

    @SerializedName("images")
    val images: List<SpotifyImageDto>?,

    @SerializedName("name")
    val name: String,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("release_date_precision")
    val releaseDatePrecision: String?,

    @SerializedName("total_tracks")
    val totalTracks: Int?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("uri")
    val uri: String?
)
