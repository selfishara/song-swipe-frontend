package org.ilerna.song_swipe_frontend.data.datasource.remote.impl

import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.PlaylistTracksResponseDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySimplifiedPlaylistDto
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
            val response = spotifyApi.getPlaylistTracks(
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
            // <- aquí llama al que devuelve PlaylistTracksResponseDto (tu segundo endpoint)
            ApiResponse.Success(response)
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }
}