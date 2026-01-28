package org.ilerna.song_swipe_frontend.data.repository.impl

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyTrackMapper
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.repository.PlaylistRepository

/**
 * Implementation of PlaylistRepository that fetches playlist tracks from Spotify API
 */
class PlaylistRepositoryImpl(
    private val spotifyApi: SpotifyApi
) : PlaylistRepository {

    override suspend fun getPlaylistTracks(playlistId: String):  NetworkResult<List<Track>> {
        return try {
            // Call to Spotify API to get playlist tracks
            val response = spotifyApi.getPlaylistTracks(playlistId)
            // Extract and map tracks
            // Convert DTOs to domain models
            val tracks = response.items.mapNotNull { item ->
                item.track?.let {
                    SpotifyTrackMapper.toDomain(it)
                }
            }
            // Return successful result with tracks
            NetworkResult.Success(tracks)
        } catch (e: Exception) {
            // In case of error, return failure result
            NetworkResult.Error(e.message ?: "Unknown error fetching playlist tracks")
        }
    }
}