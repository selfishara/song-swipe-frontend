package org.ilerna.song_swipe_frontend.domain.repository

import org.ilerna.song_swipe_frontend.core.network.NetworkResult

interface SkipRepository {

    suspend fun saveSkip(trackId: String): NetworkResult<Unit>

    suspend fun getSkippedTrackIds(): NetworkResult<List<String>>
}