package org.ilerna.song_swipe_frontend.domain.usecase.tracks

import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.repository.AuthRepository
import org.ilerna.song_swipe_frontend.domain.repository.DefaultPlaylistRepository
import org.ilerna.song_swipe_frontend.domain.repository.SpotifyRepository

/**
 * Use case for retrieving tracks from the user's default playlist.
 *
 * This use case:
 * 1. Gets the currently authenticated user.
 * 2. Retrieves the default playlist stored in Supabase.
 * 3. Fetches the playlist tracks from Spotify.
 * 4. Returns the tracks as clean domain models.
 */

class GetDefaultPlaylistItemsUseCase(
    private val authRepository: AuthRepository,
    private val defaultPlaylistRepository: DefaultPlaylistRepository,
    private val spotifyRepository: SpotifyRepository
) {
    /**
     * Executes the use case.
     *
     * @return NetworkResult containing a list of Track or an error.
     */
    suspend operator fun invoke(): NetworkResult<List<Track>>{
        // Get current authenticated user
        val user = authRepository.getCurrentUser()
            ?: return NetworkResult.Error(
                message = "User not authenticated",
                code = null
            )
        //Get default playlist from Supabase
        return when (val defaultPlaylistResult = defaultPlaylistRepository.getDefaultPlaylist(user.id)){
            is NetworkResult.Success -> {
                val playlist = defaultPlaylistResult.data
                    ?: return NetworkResult.Success(emptyList())
        //Fetch tracks from Spotify
                spotifyRepository.getPlaylistTracksDto(playlist.id)
            }
            is NetworkResult.Error -> {
                NetworkResult.Error(
                    message = defaultPlaylistResult.message,
                    code = defaultPlaylistResult.code
                )
            }
            is NetworkResult.Loading -> {
                NetworkResult.Loading
            }
        }
    }
}