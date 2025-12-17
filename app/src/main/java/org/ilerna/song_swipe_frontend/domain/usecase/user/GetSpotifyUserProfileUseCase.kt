package org.ilerna.song_swipe_frontend.domain.usecase.user

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Use case for fetching the current user's Spotify profile
 * Encapsulates the business logic for retrieving user profile data from Spotify API
 */
class GetSpotifyUserProfileUseCase(
    private val spotifyRepository: SpotifyRepository
) {

    /**
     * Executes the use case to fetch user profile from Spotify
     *
     * @return NetworkResult containing User data or error
     */
    suspend operator fun invoke(): NetworkResult<User> {
        return spotifyRepository.getCurrentUserProfile()
    }
}