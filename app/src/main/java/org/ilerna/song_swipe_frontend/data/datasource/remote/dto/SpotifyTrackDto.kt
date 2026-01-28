package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotif Tracks
 */
data class SpotifyTrackDto(
    val id: String,
    val name: String,

    @SerializedName("preview_url")
    val previewUrl: String?,

    val artists: List<SpotifyArtistDto>,
    val album: SpotifyAlbumDto,


    @SerializedName("duration_ms")
    val durationMs: Int,

    val uri: String,

    @SerializedName("is_playable")
    val isPlayable: Boolean? = null,

    val type: String
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

