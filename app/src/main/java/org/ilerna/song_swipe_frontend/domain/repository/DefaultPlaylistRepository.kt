package org.ilerna.song_swipe_frontend.domain.repository

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist

/**
 * Repository for managing the user's default playlist persisted in Supabase.
 * This allows us to store and retrieve the Spotify playlist ID so we can
 * interact with it (add items, get items, etc.) across sessions.
 */
interface DefaultPlaylistRepository {

    /**
     * Gets the user's default playlist from Supabase, if one exists.
     */
    suspend fun getDefaultPlaylist(userId: String): NetworkResult<Playlist?>

    /**
     * Saves a default playlist reference in Supabase.
     */
    suspend fun saveDefaultPlaylist(
        userId: String,
        spotifyPlaylistId: String,
        playlistName: String,
        playlistUrl: String?
    ): NetworkResult<Unit>

    /**
     * Deletes the default playlist reference from Supabase.
     * Note: this does NOT delete the playlist from Spotify.
     */
    suspend fun deleteDefaultPlaylist(userId: String): NetworkResult<Unit>
}
