package org.ilerna.song_swipe_frontend.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO that maps to the `user_playlists` table in Supabase.
 * Used for persisting the user's default Spotify playlist ID.
 */
@Serializable
data class SupabaseUserPlaylistDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("spotify_playlist_id") val spotifyPlaylistId: String,
    @SerialName("playlist_name") val playlistName: String,
    @SerialName("playlist_url") val playlistUrl: String? = null,
    @SerialName("is_default") val isDefault: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
