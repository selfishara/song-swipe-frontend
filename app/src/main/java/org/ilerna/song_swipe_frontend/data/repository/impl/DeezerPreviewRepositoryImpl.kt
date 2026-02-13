package org.ilerna.song_swipe_frontend.data.repository.impl

import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.DeezerDataSourceImpl
import org.ilerna.song_swipe_frontend.domain.repository.PreviewRepository

/**
 * Implementation of PreviewRepository using Deezer's public API.
 *
 * Searches for a matching track on Deezer and returns the 30-second preview URL.
 * Falls back gracefully if the track is not found on Deezer.
 */
class DeezerPreviewRepositoryImpl(
    private val deezerDataSource: DeezerDataSourceImpl
) : PreviewRepository {

    /**
     * Searches Deezer for a track and returns its preview URL.
     *
     * @param trackName The name of the track to search for
     * @param artistName The artist name for more accurate results
     * @return NetworkResult containing the preview URL or null if not found
     */
    override suspend fun getPreviewUrl(
        trackName: String,
        artistName: String
    ): NetworkResult<String?> {
        return when (val response = deezerDataSource.searchTrackPreview(trackName, artistName)) {
            is ApiResponse.Success -> {
                val previewUrl = response.data.data.firstOrNull()?.preview
                NetworkResult.Success(previewUrl)
            }

            is ApiResponse.Error -> {
                // Don't treat Deezer errors as fatal - preview is optional
                NetworkResult.Success(null)
            }
        }
    }
}
