package org.ilerna.song_swipe_frontend.data.repository.impl

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyAddItemsRequestDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyCreatePlaylistRequestDto
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyPlaylistMapper
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyTrackMapper
import org.ilerna.song_swipe_frontend.domain.model.Playlist
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
    override suspend fun createPlaylist(
        userId: String,
        name: String,
        description: String?,
        isPublic: Boolean
    ): NetworkResult<Playlist> {
        return try {
            val request = SpotifyCreatePlaylistRequestDto(
                name = name,
                description = description,
                isPublic = isPublic
            )
            
            val response = spotifyApi.createPlaylist(userId, request)
            
            val playlist = SpotifyPlaylistMapper.toDomain(response)
            
            NetworkResult.Success(playlist)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error creating playlist")
        }
    }

    override suspend fun addItemsToPlaylist(
        playlistId: String,
        trackIds: List<String>
    ): NetworkResult<String> {
        return try {
            val uris = trackIds.map { "spotify:track:$it" }
            val request = SpotifyAddItemsRequestDto(uris = uris)

            val response = spotifyApi.addItemsToPlaylist(playlistId, request)

            if (!response.isSuccessful) {
                return NetworkResult.Error(
                    message = "Failed to add items to playlist: ${response.code()} ${response.message()}",
                    code = response.code()
                )
            }

            val snapshotId = response.body()?.snapshotId
                ?: return NetworkResult.Error(
                    message = "Empty response body from Spotify",
                    code = response.code()
                )
            NetworkResult.Success(snapshotId)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error adding items to playlist")
        }
    }
}