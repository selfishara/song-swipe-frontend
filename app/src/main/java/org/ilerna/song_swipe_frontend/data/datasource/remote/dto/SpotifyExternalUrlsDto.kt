package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO that represents external Spotify URLs.
 * Used to access the playlist directly on Spotify.
 */
data class SpotifyExternalUrlsDto(
    @SerializedName("spotify")
    val spotify: String?
)
