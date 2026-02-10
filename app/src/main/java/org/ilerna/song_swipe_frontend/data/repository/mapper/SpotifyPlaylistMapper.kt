package org.ilerna.song_swipe_frontend.data.repository.mapper

import org.ilerna.song_swipe_frontend.data.remote.dto.response.SpotifyCreatePlaylistResponseDto
import org.ilerna.song_swipe_frontend.domain.model.Playlist
/**
 * Mapper object to convert Spotify playlist DTOs to domain models.
 */
object SpotifyPlaylistMapper {
    fun toDomain(dto: SpotifyCreatePlaylistResponseDto): Playlist {
        return Playlist(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            isPublic = dto.isPublic,
            externalUrl = dto.externalUrls
        )
    }
}
