package org.ilerna.song_swipe_frontend.data.remote.dto.response

import com.google.gson.annotations.SerializedName
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyExternalUrlsDto

/**
 * DTO for Spotify create playlist response
 */
data class SpotifyCreatePlaylistResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("public") val isPublic: Boolean,
    @SerializedName("external_urls") val externalUrls: SpotifyExternalUrlsDto?
)