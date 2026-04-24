package org.ilerna.song_swipe_frontend.data.repository.impl

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTracksResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.SpotifyDataSourceImpl
import org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyUserMapper
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyTrackMapper
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyPlaylistMapper
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository
import org.ilerna.song_swipe_frontend.domain.model.Track

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
                    FirebaseCrashlytics.getInstance().recordException(e)
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
                    val tracks = apiResponse.data.items.filter { !it.isLocal && it.track != null }
                        .map { item -> SpotifyTrackMapper.toDomain(item.track!!) }
                    NetworkResult.Success(tracks)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
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
     * Fetches tracks from multiple playlists in parallel (max 3 concurrent requests),
     * deduplicates by track ID, shuffles, and returns up to [GenrePlaylistProvider.DEFAULT_SET_SIZE] tracks.
     *
     * Each playlist is fully paginated via [SpotifyDataSourceImpl.getAllTracksForPlaylist], so
     * playlists with more than 50 tracks are retrieved in their entirety before aggregation.
     * If an individual playlist request fails, it is silently skipped so that a single
     * unavailable playlist does not abort the whole set.
     */
    override suspend fun getMultiPlaylistTracks(
        playlistIds: List<String>
    ): NetworkResult<List<Track>> {
        if (playlistIds.isEmpty()) {
            return NetworkResult.Error(message = "No playlist IDs provided", code = null)
        }

        return try {
            // Semaphore limits concurrency to 3 simultaneous playlist fetches to avoid
            // overwhelming the Spotify API rate limits while still fetching in parallel.
            val semaphore = Semaphore(3)
            val allTracks = coroutineScope {
                playlistIds.map { playlistId ->
                    async {
                        semaphore.withPermit {
                            spotifyDataSource.getAllTracksForPlaylist(playlistId)
                        }
                    }
                }.flatMap { deferred ->
                    when (val result = deferred.await()) {
                        // Map each valid track item to a domain Track, skipping local tracks
                        // and null entries (Spotify can return null for unavailable tracks).
                        is ApiResponse.Success -> result.data
                            .filter { item -> !item.isLocal && item.track != null }
                            .map { item -> SpotifyTrackMapper.toDomain(item.track!!) }
                        // Skip playlists that returned an error rather than failing the whole call.
                        is ApiResponse.Error -> emptyList()
                    }
                }
            }

            // Deduplicate across playlists, shuffle for variety, then cap at DEFAULT_SET_SIZE.
            val set = allTracks
                .distinctBy { it.id }
                .shuffled()
                .take(GenrePlaylistProvider.DEFAULT_SET_SIZE)

            NetworkResult.Success(set)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            NetworkResult.Error(message = "Failed to aggregate tracks: ${e.message}", code = null)
        }
    }

    /**
     * Streams tracks from multiple playlists. Each playlist contributes **one page** of up
     * to 50 tracks fetched at a random offset from [PAGE_OFFSET_CHOICES] — this gives
     * cross-session variety without paying the cost of full pagination.
     *
     * The first playlist to return seeds a shuffled cumulative list; every subsequent
     * arrival appends new unique tracks to the tail (no reshuffle), so the UI can render
     * early cards and the deck grows stably as more playlists come in.
     *
     * Individual playlist failures (network error or a playlist shorter than the random
     * offset, including after a fallback retry at offset=0) are skipped silently. When
     * every playlist yields zero tracks, a final empty [NetworkResult.Success] is emitted
     * so the UI can exit its loading state instead of hanging on a spinner.
     */
    override fun streamMultiPlaylistTracks(
        playlistIds: List<String>,
        maxTotal: Int
    ): Flow<NetworkResult<List<Track>>> = channelFlow {
        if (playlistIds.isEmpty()) {
            send(NetworkResult.Error(message = "No playlist IDs provided", code = null))
            return@channelFlow
        }

        // 6 concurrent is well under Spotify's ~30 req/sec rate limit but tangibly faster
        // than the older Semaphore(3) used by the blocking aggregate.
        val semaphore = Semaphore(STREAM_CONCURRENCY)
        val mutex = Mutex()
        val cumulative = linkedMapOf<String, Track>()
        var seeded = false

        try {
            coroutineScope {
                playlistIds.forEach { playlistId ->
                    launch {
                        semaphore.withPermit {
                            val tracks = fetchSinglePageWithRandomOffset(playlistId)
                            if (tracks.isEmpty()) return@withPermit

                            val snapshot = mutex.withLock {
                                if (!seeded) {
                                    tracks.shuffled().forEach { t ->
                                        cumulative.putIfAbsent(t.id, t)
                                    }
                                    seeded = true
                                } else {
                                    tracks.forEach { t ->
                                        cumulative.putIfAbsent(t.id, t)
                                    }
                                }
                                cumulative.values.take(maxTotal).toList()
                            }

                            send(NetworkResult.Success(snapshot))
                        }
                    }
                }
            }

            if (cumulative.isEmpty()) {
                send(NetworkResult.Success(emptyList()))
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            send(NetworkResult.Error(message = "Failed to stream tracks: ${e.message}", code = null))
        }
    }

    /**
     * Fetches a single page of tracks at a random offset. If the chosen offset overshoots
     * the playlist (empty items or error) and offset > 0, retries once with offset=0.
     */
    private suspend fun fetchSinglePageWithRandomOffset(playlistId: String): List<Track> {
        val offset = PAGE_OFFSET_CHOICES.random()
        val primary = extractDomainTracks(
            spotifyDataSource.getPlaylistTracks(playlistId, limit = PAGE_SIZE, offset = offset)
        )
        if (primary.isNotEmpty() || offset == 0) return primary

        return extractDomainTracks(
            spotifyDataSource.getPlaylistTracks(playlistId, limit = PAGE_SIZE, offset = 0)
        )
    }

    private fun extractDomainTracks(
        response: ApiResponse<SpotifyTracksResponse>
    ): List<Track> = when (response) {
        is ApiResponse.Success -> response.data.items
            .filter { !it.isLocal && it.track != null }
            .map { SpotifyTrackMapper.toDomain(it.track!!) }
        is ApiResponse.Error -> emptyList()
    }

    /**
     * Gets all playlists owned or followed by the current user.
     * Fetches all pages from the Spotify API and maps DTOs to domain models.
     */
    override suspend fun getUserPlaylists(): NetworkResult<List<Playlist>> {
        return when (val apiResponse = spotifyDataSource.getAllUserPlaylists()) {
            is ApiResponse.Success -> {
                try {
                    val playlists = apiResponse.data.map { dto ->
                        SpotifyPlaylistMapper.toDomain(dto)
                    }
                    NetworkResult.Success(playlists)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    NetworkResult.Error(
                        message = "Failed to process user playlists: ${e.message}",
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
                    FirebaseCrashlytics.getInstance().recordException(e)
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

    /**
     * Removes items (tracks) from a Spotify playlist.
     * Converts API response to NetworkResult and extracts snapshot ID on success.
     */
    override suspend fun removeItemsFromPlaylist(
        playlistId: String,
        trackIds: List<String>
    ): NetworkResult<String> {
        if (trackIds.isEmpty()) {
            return NetworkResult.Error(
                message = "No tracks to remove from playlist",
                code = null
            )
        }

        return when (val apiResponse = spotifyDataSource.removeItemsFromPlaylist(playlistId, trackIds)) {
            is ApiResponse.Success -> {
                try {
                    val snapshotId = apiResponse.data.snapshotId
                    NetworkResult.Success(snapshotId)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    NetworkResult.Error(
                        message = "Failed to remove items from playlist: ${e.message}",
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

    private companion object {
        const val PAGE_SIZE = 50
        const val STREAM_CONCURRENCY = 6
        val PAGE_OFFSET_CHOICES = listOf(0, 50, 100, 150)
    }
}