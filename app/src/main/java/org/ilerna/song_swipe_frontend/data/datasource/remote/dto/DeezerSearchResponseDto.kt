package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Deezer search API response.
 * Endpoint: GET https://api.deezer.com/search?q=track:"name" artist:"artist"
 *
 * Deezer's search API is public and does not require authentication.
 * Each track result includes a 30-second MP3 preview URL.
 */
data class DeezerSearchResponseDto(
    @SerializedName("data")
    val data: List<DeezerTrackDto>,

    @SerializedName("total")
    val total: Int
)

/**
 * DTO for a Deezer track object returned in search results.
 */
data class DeezerTrackDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("preview")
    val preview: String?,

    @SerializedName("artist")
    val artist: DeezerArtistDto?,

    @SerializedName("album")
    val album: DeezerAlbumDto?,

    @SerializedName("duration")
    val duration: Int
)

/**
 * DTO for a Deezer artist object.
 */
data class DeezerArtistDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String
)

/**
 * DTO for a Deezer album object.
 */
data class DeezerAlbumDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("cover_medium")
    val coverMedium: String?
)
