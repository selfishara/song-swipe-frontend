package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.PreviewRepository

/**
 * Use case that retrieves a 30-second audio preview URL for a track.
 *
 * Since Spotify deprecated preview_url, this use case fetches previews
 * from an alternative source (Deezer) via the PreviewRepository.
 *
 * @param previewRepository Repository that provides preview URLs
 */
class GetTrackPreviewUseCase(
    private val previewRepository: PreviewRepository
) {

    /**
     * Searches for a preview URL for the given track.
     *
     * @param trackName Name of the track
     * @param artistName Name of the primary artist
     * @return NetworkResult containing the preview URL (nullable if not found)
     */
    suspend operator fun invoke(
        trackName: String,
        artistName: String
    ): NetworkResult<String?> {
        require(trackName.isNotBlank()) { "Track name cannot be empty" }
        require(artistName.isNotBlank()) { "Artist name cannot be empty" }
        return previewRepository.getPreviewUrl(trackName, artistName)
    }
}
