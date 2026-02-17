package org.ilerna.song_swipe_frontend.data.repository.impl

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import org.ilerna.song_swipe_frontend.core.config.SupabaseConfig
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SupabaseUserPlaylistDto
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.repository.DefaultPlaylistRepository

/**
 * Implementation of [DefaultPlaylistRepository] using Supabase Postgrest.
 * Manages the `user_playlists` table to persist the default playlist reference.
 */
class SupabaseDefaultPlaylistRepository(
    private val supabaseClient: SupabaseClient = SupabaseConfig.client
) : DefaultPlaylistRepository {

    companion object {
        private const val TAG = "SupabaseDefaultPlaylist"
        private const val TABLE_NAME = "user_playlists"
    }

    override suspend fun getDefaultPlaylist(userId: String): NetworkResult<Playlist?> {
        return try {
            val result = supabaseClient.postgrest
                .from(TABLE_NAME)
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_default", true)
                    }
                }
                .decodeList<SupabaseUserPlaylistDto>()

            val dto = result.firstOrNull()
            if (dto != null) {
                val playlist = Playlist(
                    id = dto.spotifyPlaylistId,
                    name = dto.playlistName,
                    description = null,
                    url = dto.playlistUrl,
                    imageUrl = null,
                    isPublic = false,
                    externalUrl = dto.playlistUrl ?: ""
                )
                Log.d(TAG, "Default playlist found: ${playlist.name} (${playlist.id})")
                NetworkResult.Success(playlist)
            } else {
                Log.d(TAG, "No default playlist found for user $userId")
                NetworkResult.Success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting default playlist: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Error getting default playlist")
        }
    }

    override suspend fun saveDefaultPlaylist(
        userId: String,
        spotifyPlaylistId: String,
        playlistName: String,
        playlistUrl: String?
    ): NetworkResult<Unit> {
        return try {
            val dto = SupabaseUserPlaylistDto(
                userId = userId,
                spotifyPlaylistId = spotifyPlaylistId,
                playlistName = playlistName,
                playlistUrl = playlistUrl,
                isDefault = true
            )

            supabaseClient.postgrest
                .from(TABLE_NAME)
                .insert(dto)

            Log.d(TAG, "Default playlist saved: $playlistName ($spotifyPlaylistId)")
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving default playlist: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Error saving default playlist")
        }
    }

    override suspend fun deleteDefaultPlaylist(userId: String): NetworkResult<Unit> {
        return try {
            supabaseClient.postgrest
                .from(TABLE_NAME)
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("is_default", true)
                    }
                }

            Log.d(TAG, "Default playlist deleted for user $userId")
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting default playlist: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Error deleting default playlist")
        }
    }
}
