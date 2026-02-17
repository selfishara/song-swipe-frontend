package org.ilerna.song_swipe_frontend.domain.repository

import org.ilerna.song_swipe_frontend.core.network.NetworkResult

/**
 * Repository interface for obtaining track preview URLs from an external source.
 *
 * Since Spotify deprecated preview_url, this repository provides an abstraction
 * to fetch 30-second preview URLs from other sources - Currently Deezer's public API.
 */
interface PreviewRepository {

    /**
     * Gets a 30-second preview URL for a track.
     *
     * @param trackName The name of the track
     * @param artistName The name of the primary artist
     * @return NetworkResult containing the preview URL string, or null if not found
     */
    suspend fun getPreviewUrl(trackName: String, artistName: String): NetworkResult<String?>
}
