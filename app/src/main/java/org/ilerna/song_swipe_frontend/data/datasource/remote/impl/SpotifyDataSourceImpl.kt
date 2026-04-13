package org.ilerna.song_swipe_frontend.data.datasource.remote.impl

import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyAddItemsRequestDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyRemoveItemsRequestDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.PlaylistTracksResponseDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyPlaylistItemDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySimplifiedPlaylistDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySnapshotResponseDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTracksResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto
import kotlin.collections.emptyList

/**
 * Implementation of Spotify data source
 * Handles direct API calls to Spotify Web API
 */
class SpotifyDataSourceImpl(
    private val spotifyApi: SpotifyApi
) {

    /**
     * Fetches the current user's profile from Spotify API
     *
     * @return ApiResponse containing SpotifyUserDto or error
     */
    suspend fun getCurrentUserProfile(): ApiResponse<SpotifyUserDto> {
        return try {
            val response = spotifyApi.getCurrentUserProfile()
            ApiResponse.create(response)
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }

    suspend fun getPlaylistTracks(
        playlistId: String,
        limit: Int = 50,
        offset: Int = 0,
        market: String? = null
    ): ApiResponse<SpotifyTracksResponse> {
        return try {
            val response = spotifyApi.getPlaylistTracksPaged(
                playlistId = playlistId,
                limit = limit,
                offset = offset,
                market = market
            )
            ApiResponse.create(response)
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }

    /**
     * Fetches Spotify playlists by genre using Spotify Browse categories.
     *
     * @param genre Genre or category name (e.g. "pop", "rock")
     * @return ApiResponse containing a list of SpotifySimplifiedPlaylistDto
     */
    suspend fun getPlaylistsByGenre(
        genre: String
    ): ApiResponse<List<SpotifySimplifiedPlaylistDto>> {
        return try {
            val normalizedGenre = genre.trim().lowercase()

            val categoriesApiResponse = ApiResponse.create(spotifyApi.getCategories())

            if (categoriesApiResponse is ApiResponse.Error) {
                return categoriesApiResponse
            }

            val categories = (categoriesApiResponse as ApiResponse.Success)
                .data.categories.items

            val category = categories.firstOrNull {
                it.id.lowercase() == normalizedGenre ||
                        it.name.lowercase() == normalizedGenre
            } ?: return ApiResponse.Success(emptyList())

            val playlistsApiResponse = ApiResponse.create(
                spotifyApi.getCategoryPlaylists(category.id)
            )

            if (playlistsApiResponse is ApiResponse.Error) {
                return playlistsApiResponse
            }

            val playlists = (playlistsApiResponse as ApiResponse.Success)
                .data.playlists.items

            ApiResponse.Success(playlists)
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }

    /**
     * Adds items (tracks) to a Spotify playlist.
     *
     * @param playlistId The Spotify ID of the playlist
     * @param trackIds A list of Spotify track IDs to add
     * @return ApiResponse containing SpotifySnapshotResponseDto or error
     */
    suspend fun addItemsToPlaylist(
        playlistId: String,
        trackIds: List<String>
    ): ApiResponse<SpotifySnapshotResponseDto> {
        return try {
            val uris = trackIds.map { "spotify:track:$it" }
            val body = SpotifyAddItemsRequestDto(uris = uris)
            val response = spotifyApi.addItemsToPlaylist(
                playlistId = playlistId,
                body = body
            )
            ApiResponse.create(response)
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }

    /**
     * Removes items (tracks) from a Spotify playlist.
     *
     * @param playlistId The Spotify ID of the playlist
     * @param trackIds A list of Spotify track IDs to remove
     * @return ApiResponse containing SpotifySnapshotResponseDto or error
     */
    suspend fun removeItemsFromPlaylist(
        playlistId: String,
        trackIds: List<String>
    ): ApiResponse<SpotifySnapshotResponseDto> {
        return try {
            val tracks = trackIds.map { SpotifyRemoveItemsRequestDto.TrackUri("spotify:track:$it") }
            val body = SpotifyRemoveItemsRequestDto(tracks = tracks)
            val response = spotifyApi.removeItemsFromPlaylist(
                playlistId = playlistId,
                body = body
            )
            ApiResponse.create(response)
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }

    /**
     * Fetches tracks of a specific Spotify playlist.
     *
     * @param playlistId The Spotify ID of the playlist
     * @return ApiResponse containing PlaylistTracksResponseDto
     */
    suspend fun getPlaylistTracksDto(
        playlistId: String,
        limit: Int = 50,
        offset: Int = 0,
        market: String? = null
    ): ApiResponse<PlaylistTracksResponseDto> {
        return try {
            val response = spotifyApi.getPlaylistTracks(playlistId)
            ApiResponse.Success(response)
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }

    /**
     * Fetches ALL tracks from a playlist by paginating until there are no more pages.
     *
     * @param playlistId The Spotify ID of the playlist
     * @param pageLimit Number of tracks to request per page (max 50 per Spotify API)
     * @return ApiResponse containing the full flat list of [SpotifyPlaylistItemDto]
     */
    suspend fun getAllTracksForPlaylist(
        playlistId: String,
        pageLimit: Int = 50
    ): ApiResponse<List<SpotifyPlaylistItemDto>> {
        return try {
            val allItems = mutableListOf<SpotifyPlaylistItemDto>()
            var offset = 0

            while (true) {
                val response = spotifyApi.getPlaylistTracksPaged(
                    playlistId = playlistId,
                    limit = pageLimit,
                    offset = offset
                )
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    if (allItems.isEmpty()) {
                        return ApiResponse.Error(
                            code = response.code(),
                            message = response.message().ifBlank { "Empty response from server" },
                            errorBody = response.errorBody()?.string()
                        )
                    }
                    break
                }
                allItems.addAll(body.items)
                if (body.next == null) break
                offset += pageLimit
            }

            ApiResponse.Success(allItems)
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }
}