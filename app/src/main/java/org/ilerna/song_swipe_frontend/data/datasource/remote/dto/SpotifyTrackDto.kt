package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotify track object
 */
data class SpotifyTrackDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("artists")
    val artists: List<SpotifyArtistDto>,

    @SerializedName("album")
    val album: SpotifyAlbumDto,

    @SerializedName("duration_ms")
    val durationMs: Int,

    @SerializedName("preview_url")
    val previewUrl: String?,

    @SerializedName("is_playable")
    val isPlayable: Boolean?,

    @SerializedName("type")
    val type: String?,
)

// --- DTOs extras de Tracks ---

data class SpotifyArtistDto(
    val id: String,
    val name: String
)

data class SpotifyAlbumDto(
    val name: String,
    val images: List<SpotifyImageDto>
)
