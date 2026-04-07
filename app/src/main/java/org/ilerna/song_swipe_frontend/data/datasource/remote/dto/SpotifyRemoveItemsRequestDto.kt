package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Spotify remove items from playlist request.
 * Used with DELETE /v1/playlists/{playlist_id}/tracks
 */
data class SpotifyRemoveItemsRequestDto(
    @SerializedName("tracks")
    val tracks: List<TrackUri>,

    @SerializedName("snapshot_id")
    val snapshotId: String? = null
) {
    data class TrackUri(
        @SerializedName("uri")
        val uri: String
    )
}
