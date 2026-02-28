package org.ilerna.song_swipe_frontend.data.datasource.remote.impl

import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyAddItemsRequestDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySimplifiedPlaylistDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySnapshotResponseDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTracksResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto

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
}