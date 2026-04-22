package org.ilerna.song_swipe_frontend.domain.usecase

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.SkipRepository

class GetSkippedTrackIdsUseCase(
    private val skipRepository: SkipRepository
) {
    suspend operator fun invoke(): NetworkResult<List<String>> {
        return skipRepository.getSkippedTrackIds()
    }
}