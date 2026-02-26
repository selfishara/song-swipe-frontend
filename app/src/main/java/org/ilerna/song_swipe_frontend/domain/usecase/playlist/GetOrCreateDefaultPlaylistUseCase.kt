package org.ilerna.song_swipe_frontend.domain.usecase.playlist

import android.util.Log
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.repository.DefaultPlaylistRepository
import org.ilerna.song_swipe_frontend.domain.repository.PlaylistRepository

/**
 * Use case that ensures a default playlist exists for the current user.
 *
 * Flow:
 * 1. Check Supabase for an existing default playlist
 * 2. If found, return it
 * 3. If not found, create one via Spotify API, persist the ID in Supabase, and return it
 */
class GetOrCreateDefaultPlaylistUseCase(
    private val defaultPlaylistRepository: DefaultPlaylistRepository,
    private val playlistRepository: PlaylistRepository
) {

    companion object {
        private const val TAG = "GetOrCreateDefaultPlaylist"
        private const val DEFAULT_PLAYLIST_NAME = "SongSwipe Likes"
        private const val DEFAULT_PLAYLIST_DESCRIPTION = "Songs you liked on SongSwipe"
    }

    /**
     * @param supabaseUserId The Supabase auth user ID (UUID)
     * @param spotifyUserId The Spotify user ID (for creating playlists via Spotify API)
     */
    suspend operator fun invoke(
        supabaseUserId: String,
        spotifyUserId: String
    ): NetworkResult<Playlist> {
        // 1. Check if a default playlist already exists in Supabase
        when (val existing = defaultPlaylistRepository.getDefaultPlaylist(supabaseUserId)) {
            is NetworkResult.Success -> {
                if (existing.data != null) {
                    Log.d(TAG, "Default playlist already exists: ${existing.data.name}")
                    return NetworkResult.Success(existing.data)
                }
                // No playlist found, continue to create one
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "Error checking for default playlist: ${existing.message}")
                return NetworkResult.Error(existing.message)
            }
            is NetworkResult.Loading -> { /* no-op */ }
        }

        // 2. Create a new playlist on Spotify
        Log.d(TAG, "Creating new default playlist on Spotify for user $spotifyUserId")
        val createResult = playlistRepository.createPlaylist(
            userId = spotifyUserId,
            name = DEFAULT_PLAYLIST_NAME,
            description = DEFAULT_PLAYLIST_DESCRIPTION,
            isPublic = false
        )

        return when (createResult) {
            is NetworkResult.Success -> {
                val playlist = createResult.data
                Log.d(TAG, "Spotify playlist created: ${playlist.name} (${playlist.id})")

                // 3. Persist in Supabase
                val saveResult = defaultPlaylistRepository.saveDefaultPlaylist(
                    userId = supabaseUserId,
                    spotifyPlaylistId = playlist.id,
                    playlistName = playlist.name,
                    playlistUrl = playlist.externalUrl
                )

                when (saveResult) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Default playlist persisted in Supabase")
                        NetworkResult.Success(playlist)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Failed to persist playlist in Supabase: ${saveResult.message}")
                        // Return the playlist anyway since it was created on Spotify
                        NetworkResult.Success(playlist)
                    }
                    is NetworkResult.Loading -> NetworkResult.Success(playlist)
                }
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "Failed to create Spotify playlist: ${createResult.message}")
                NetworkResult.Error(createResult.message)
            }
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }
}
