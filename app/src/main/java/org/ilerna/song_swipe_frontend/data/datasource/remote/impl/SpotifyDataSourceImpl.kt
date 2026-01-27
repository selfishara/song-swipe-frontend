package org.ilerna.song_swipe_frontend.data.datasource.remote.impl

import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySimplifiedPlaylistDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto
import kotlin.collections.emptyList
import retrofit2.Response


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

            // Step 1: Fetch Spotify categories (used as genres)
            val categoriesResponse = spotifyApi.getCategories()
            val categories = categoriesResponse.body()
                ?.categories
                ?.items
                ?: emptyList()

            // Step 2: Find matching category by id or name
            val category = categories.firstOrNull {
                it.id.lowercase() == normalizedGenre ||
                        it.name.lowercase() == normalizedGenre
            } ?: return ApiResponse.create(
                Response.success(emptyList())
            )

            // Step 3: Fetch playlists for the matched category
            val playlistsResponse =
                spotifyApi.getCategoryPlaylists(category.id)

            val playlists = playlistsResponse.body()
                ?.playlists
                ?.items
                ?: emptyList()

            ApiResponse.create(Response.success(playlists))
        } catch (e: Exception) {
            ApiResponse.create(e)
        }
    }


}