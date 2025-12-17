package org.ilerna.song_swipe_frontend.data.repository.mapper

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyImageDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto
import org.ilerna.song_swipe_frontend.domain.model.User

/**
 * Mapper to convert Spotify API DTOs to domain models
 */
object SpotifyUserMapper {

    /**
     * Converts SpotifyUserDto to User domain model
     * Selects the largest available profile image if multiple are present
     *
     * @param dto The Spotify user DTO from the API
     * @return User domain model
     */
    fun toDomain(dto: SpotifyUserDto): User {
        return User(
            id = dto.id,
            email = dto.email ?: "",
            displayName = dto.displayName ?: dto.id,
            profileImageUrl = selectBestImage(dto.images),
            spotifyId = dto.id
        )
    }

    /**
     * Selects the best image from a list of Spotify images
     * Prefers larger images for better quality
     *
     * @param images List of Spotify images, or null
     * @return URL of the selected image, or null if no images available
     */
    private fun selectBestImage(images: List<SpotifyImageDto>?): String? {
        if (images.isNullOrEmpty()) return null

        // Return the first image with the largest dimension
        // Spotify returns images sorted by size (largest first)
        return images.firstOrNull()?.url
    }
}