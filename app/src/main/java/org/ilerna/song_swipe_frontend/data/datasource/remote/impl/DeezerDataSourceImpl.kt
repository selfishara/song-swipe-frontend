package org.ilerna.song_swipe_frontend.data.datasource.remote.impl

import android.util.Log
import org.ilerna.song_swipe_frontend.data.datasource.remote.api.DeezerApi
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.DeezerSearchResponseDto
import org.ilerna.song_swipe_frontend.core.network.ApiResponse

/**
 * DataSource implementation for Deezer API.
 *
 * Handles the network call to search for tracks and retrieve
 * 30-second preview URLs from Deezer's public API.
 */
class DeezerDataSourceImpl(
    private val deezerApi: DeezerApi
) {

    companion object {
        private const val TAG = "DeezerDataSource"
    }

    /**
     * Searches for a track on Deezer to obtain its preview URL.
     *
     * Uses Deezer's advanced search syntax for better accuracy:
     * track:"song name" artist:"artist name"
     *
     * @param trackName The name of the track
     * @param artistName The name of the artist for more accurate matching
     * @param limit Maximum number of results (default 1)
     * @return ApiResponse containing the search results or an error
     */
    suspend fun searchTrackPreview(
        trackName: String,
        artistName: String,
        limit: Int = 1
    ): ApiResponse<DeezerSearchResponseDto> {
        return try {
            // Use Deezer advanced search syntax for precise matching
            val query = "track:\"$trackName\" artist:\"$artistName\""
            Log.d(TAG, "Searching Deezer for: $query")

            val response = deezerApi.searchTrack(query, limit)
            ApiResponse.create(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching Deezer: ${e.message}")
            ApiResponse.create(e)
        }
    }
}
