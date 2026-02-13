package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO that represents the response from Spotify API when fetching playlists
 * of a specific category.
 */
data class SpotifyCategoryPlaylistsResponseDto(
    @SerializedName("playlists")
    val playlists: SpotifyPlaylistsPagingDto
)

/**
 * DTO that represents paginated playlists data from Spotify.
 */
data class SpotifyPlaylistsPagingDto(
    @SerializedName("items")
    val items: List<SpotifySimplifiedPlaylistDto>
)
