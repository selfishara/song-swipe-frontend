package org.ilerna.song_swipe_frontend.data.repository.impl

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
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
        val trace = FirebasePerformance.getInstance().newTrace("spotify_get_current_user")
        trace.start()

        return try {
            when (val apiResponse = spotifyDataSource.getCurrentUserProfile()) {
                is ApiResponse.Success -> {
                    try {
                        val user = SpotifyUserMapper.toDomain(apiResponse.data)
                        trace.putAttribute("status", "success")
                        NetworkResult.Success(user)
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        trace.putAttribute("status", "mapping_error")
                        NetworkResult.Error(
                            message = "Failed to process user profile: ${e.message}",
                            code = null
                        )
                    }
                }

                is ApiResponse.Error -> {
                    trace.putAttribute("status", "api_error")
                    NetworkResult.Error(
                        message = apiResponse.message,
                        code = apiResponse.code
                    )
                }
            }
        } finally {
            trace.stop()
        }
    }

    override suspend fun getPlaylistTracks(playlistId: String): NetworkResult<List<Track>> {
        val trace = FirebasePerformance.getInstance().newTrace("spotify_get_playlist_tracks")
        trace.start()

        return try {
            when (val apiResponse = spotifyDataSource.getPlaylistTracks(playlistId)) {
                is ApiResponse.Success -> {
                    try {
                        val tracks = apiResponse.data.items.filter { !it.isLocal && it.track != null }
                            .map { item -> SpotifyTrackMapper.toDomain(item.track!!) }
                        trace.putAttribute("status", "success")
                        NetworkResult.Success(tracks)
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        trace.putAttribute("status", "mapping_error")
                        NetworkResult.Error(
                            message = "Failed to get tracks: ${e.message}",
                            code = null
                        )
                    }
                }

                is ApiResponse.Error -> {
                    trace.putAttribute("status", "api_error")
                    NetworkResult.Error(
                        message = apiResponse.message,
                        code = apiResponse.code
                    )
                }
            }
        } finally {
            trace.stop()
        }
    }

    /**
     * Gets Spotify playlists by genre.
     * Converts API response DTOs into domain Playlist models.
     */
    override suspend fun getPlaylistsByGenre(
        genre: String
    ): NetworkResult<List<Playlist>> {
        val trace = FirebasePerformance.getInstance().newTrace("spotify_get_playlists_by_genre")
        trace.start()

        return try {
            when (val apiResponse = spotifyDataSource.getPlaylistsByGenre(genre)) {
                is ApiResponse.Success -> {
                    try {
                        val playlists = apiResponse.data.map {
                            SpotifyPlaylistMapper.toDomain(it)
                        }
                        trace.putAttribute("status", "success")
                        NetworkResult.Success(playlists)
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        trace.putAttribute("status", "mapping_error")
                        NetworkResult.Error(
                            message = "Failed to process playlists: ${e.message}",
                            code = null
                        )
                    }
                }
                is ApiResponse.Error -> {
                    trace.putAttribute("status", "api_error")
                    NetworkResult.Error(
                        message = apiResponse.message,
                        code = apiResponse.code
                    )
                }
            }
        } finally {
            trace.stop()
        }
    }

    /**
     * Gets playlist tracks using DTO response from datasource.
     * Converts ApiResponse to NetworkResult and maps DTO to domain Track model.
     *
     * @param playlistId The Spotify ID of the playlist
     * @return NetworkResult containing list of Track or error
     */
    override suspend fun getPlaylistTracksDto(
        playlistId: String
    ): NetworkResult<List<Track>> {
        val trace = FirebasePerformance.getInstance().newTrace("spotify_get_playlist_tracks_dto")
        trace.start()

        return try {
            when (val apiResponse = spotifyDataSource.getPlaylistTracksDto(playlistId)) {
                is ApiResponse.Success -> {
                    try {
                        val tracks = apiResponse.data.items
                            .mapNotNull { it.track }
                            .map { SpotifyTrackMapper.toDomain(it) }

                        trace.putAttribute("status", "success")
                        NetworkResult.Success(tracks)
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        trace.putAttribute("status", "mapping_error")
                        NetworkResult.Error(
                            message = "Failed to get tracks: ${e.message}",
                            code = null
                        )
                    }
                }

                is ApiResponse.Error -> {
                    trace.putAttribute("status", "api_error")
                    NetworkResult.Error(
                        message = apiResponse.message,
                        code = apiResponse.code
                    )
                }
            }
        } finally {
            trace.stop()
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
        val trace = FirebasePerformance.getInstance().newTrace("spotify_add_items_to_playlist")
        trace.start()

        if (trackIds.isEmpty()) {
            trace.putAttribute("status", "empty_request")
            trace.stop()
            return NetworkResult.Error(
                message = "No tracks to add to playlist",
                code = null
            )
        }

        return try {
            when (val apiResponse = spotifyDataSource.addItemsToPlaylist(playlistId, trackIds)) {
                is ApiResponse.Success -> {
                    try {
                        val snapshotId = apiResponse.data.snapshotId
                        trace.putAttribute("status", "success")
                        NetworkResult.Success(snapshotId)
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                        trace.putAttribute("status", "mapping_error")
                        NetworkResult.Error(
                            message = "Failed to add items to playlist: ${e.message}",
                            code = null
                        )
                    }
                }

                is ApiResponse.Error -> {
                    trace.putAttribute("status", "api_error")
                    NetworkResult.Error(
                        message = apiResponse.message,
                        code = apiResponse.code
                    )
                }
            }
        } finally {
            trace.stop()
        }
    }
}