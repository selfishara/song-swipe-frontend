package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotify create playlist request
 */
data class SpotifyCreatePlaylistRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("public") val isPublic: Boolean = false
)