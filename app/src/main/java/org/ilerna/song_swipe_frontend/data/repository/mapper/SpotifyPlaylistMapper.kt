package org.ilerna.song_swipe_frontend.data.repository.mapper

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySimplifiedPlaylistDto
import org.ilerna.song_swipe_frontend.domain.model.Playlist

/**
 * Mapper that converts Spotify playlist DTOs into domain Playlist models.
 */
object SpotifyPlaylistMapper {

    fun toDomain(dto: SpotifySimplifiedPlaylistDto): Playlist {
        return Playlist(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            url = dto.externalUrls?.spotify,
            imageUrl = dto.images?.firstOrNull()?.url
        )
    }
}
