package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotify snapshot response
 * Represents the response from Spotify API when adding items to a playlist
 */
data class SpotifySnapshotResponseDto(
    @SerializedName("snapshot_id")
    val snapshotId: String
)