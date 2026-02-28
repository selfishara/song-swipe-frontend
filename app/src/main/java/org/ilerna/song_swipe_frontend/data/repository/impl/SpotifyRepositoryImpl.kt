package org.ilerna.song_swipe_frontend.data.repository.impl

import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.SpotifyDataSourceImpl
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyPlaylistMapper
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyUserMapper
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyTrackMapper
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.ilerna.song_swipe_frontend.domain.model.Track

import org.ilerna.song_swipe_frontend.domain.model.Playlist

/**
 * Implementation of SpotifyRepository
 * Coordinates data from Spotify API and transforms it to domain models
 */
class SpotifyRepositoryImpl(
    private val spotifyDataSource: SpotifyDataSourceImpl
) : SpotifyRepository {

    /**
     * Gets the current user's Spotify profile
     * Converts ApiResponse to NetworkResult and maps DTO to domain model
     *
     * @return NetworkResult containing User or error
     */
    override suspend fun getCurrentUserProfile(): NetworkResult<User> {
        return when (val apiResponse = spotifyDataSource.getCurrentUserProfile()) {
            is ApiResponse.Success -> {
                try {
                    val user = SpotifyUserMapper.toDomain(apiResponse.data)
                    NetworkResult.Success(user)
                } catch (e: Exception) {
                    NetworkResult.Error(
                        message = "Failed to process user profile: ${e.message}",
                        code = null
                    )
                }
            }

            is ApiResponse.Error -> {
                NetworkResult.Error(
                    message = apiResponse.message,
                    code = apiResponse.code
                )
            }
        }
    }

    override suspend fun getPlaylistTracks(playlistId: String): NetworkResult<List<Track>> {
        return when (val apiResponse = spotifyDataSource.getPlaylistTracks(playlistId)) {
            is ApiResponse.Success -> {
                try {
                    val tracks =
                        apiResponse.data.items.filter { !it.isLocal && it.track != null }
                            .map { item -> SpotifyTrackMapper.toDomain(item.track!!) }
                    NetworkResult.Success(tracks)
                } catch (e: Exception) {
                    NetworkResult.Error(
                        message = "Failed to get tracks: ${e.message}",
                        code = null
                    )
                }
            }

            is ApiResponse.Error -> {
                NetworkResult.Error(
                    message = apiResponse.message,
                    code = apiResponse.code
                )
            }
        }
    }

    /**
     * Gets Spotify playlists by genre.
     * Converts API response DTOs into domain Playlist models.
     */
    override suspend fun getPlaylistsByGenre(
        genre: String
    ): NetworkResult<List<Playlist>> {
        return when (val apiResponse = spotifyDataSource.getPlaylistsByGenre(genre)) {
            is ApiResponse.Success -> {
                try {
                    val playlists = apiResponse.data.map {
                        SpotifyPlaylistMapper.toDomain(it)
                    }
                    NetworkResult.Success(playlists)
                } catch (e: Exception) {
                    NetworkResult.Error(
                        message = "Failed to process playlists: ${e.message}",
                        code = null
                    )
                }
            }
            is ApiResponse.Error -> {
                NetworkResult.Error(
                    message = apiResponse.message,
                    code = apiResponse.code
                )
            }
        }
    }
    /**
     * Adds items (tracks) to a Spotify playlist.
     * Converts API response to NetworkResult and extracts snapshot ID on success.
     */
    override suspend fun addItemsToPlaylist(
        playlistId: String,
        trackIds: List<String>
    ): NetworkResult<String> {
        if (trackIds.isEmpty()) {
            return NetworkResult.Error(
                message = "No tracks to add to playlist",
                code = null
            )
        }
        return when (val apiResponse = spotifyDataSource.addItemsToPlaylist(playlistId, trackIds)) {
            is ApiResponse.Success -> {
                try {
                    val snapshotId = apiResponse.data.snapshotId
                    NetworkResult.Success(snapshotId)
                } catch (e: Exception) {
                    NetworkResult.Error(
                        message = "Failed to add items to playlist: ${e.message}",
                        code = null
                    )
                }
            }
            is ApiResponse.Error -> {
                NetworkResult.Error(
                    message = apiResponse.message,
                    code = apiResponse.code
                )
            }
        }
    }
}