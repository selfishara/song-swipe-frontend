package org.ilerna.song_swipe_frontend.data.datasource.remote.impl

import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.SpotifyApi
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
}
