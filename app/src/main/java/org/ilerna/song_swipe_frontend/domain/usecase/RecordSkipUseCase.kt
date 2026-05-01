package org.ilerna.song_swipe_frontend.domain.usecase

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.repository.SkipRepository

class RecordSkipUseCase(
    private val skipRepository: SkipRepository
) {
    suspend operator fun invoke(trackId: String): NetworkResult<Unit> {
        return skipRepository.saveSkip(trackId)
    }
}