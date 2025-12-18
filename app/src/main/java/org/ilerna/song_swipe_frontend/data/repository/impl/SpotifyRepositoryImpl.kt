package org.ilerna.song_swipe_frontend.data.repository.impl

import org.ilerna.song_swipe_frontend.core.network.ApiResponse
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.remote.impl.SpotifyDataSourceImpl
import org.ilerna.song_swipe_frontend.data.repository.mapper.SpotifyUserMapper
import org.ilerna.song_swipe_frontend.domain.model.User
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Implementation of SpotifyRepository
 * Coordinates data from Spotify API and transforms it to domain models
 */
class SpotifyRepositoryImpl(
    private val spotifyDataSource: SpotifyDataSourceImpl
) : SpotifyRepository {

    /**
     * Gets the current user's Spotify profile
     * Converts ApiResponse to NetworkResult and maps DTO to domain model
     *
     * @return NetworkResult containing User or error
     */
    override suspend fun getCurrentUserProfile(): NetworkResult<User> {
        return when (val apiResponse = spotifyDataSource.getCurrentUserProfile()) {
            is ApiResponse.Success -> {
                try {
                    val user = SpotifyUserMapper.toDomain(apiResponse.data)
                    NetworkResult.Success(user)
                } catch (e: Exception) {
                    NetworkResult.Error(
                        message = "Failed to process user profile: ${e.message}",
                        code = null
                    )
                }
            }
            is ApiResponse.Error -> {
                NetworkResult.Error(
                    message = apiResponse.message,
                    code = apiResponse.code
                )
            }
        }
    }
}