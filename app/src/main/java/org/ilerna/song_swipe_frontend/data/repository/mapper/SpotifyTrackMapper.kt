package org.ilerna.song_swipe_frontend.data.repository.mapper

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTrackDto
import org.ilerna.song_swipe_frontend.domain.model.Album
import org.ilerna.song_swipe_frontend.domain.model.AlbumSimplified
import org.ilerna.song_swipe_frontend.domain.model.Artist
import org.ilerna.song_swipe_frontend.domain.model.Image
import org.ilerna.song_swipe_frontend.domain.model.Track

object SpotifyTrackMapper {

    fun toDomain(dto: SpotifyTrackDto): Track {
        return Track(
            id = dto.id,
            name = dto.name,
            previewUrl = dto.previewUrl,
            durationMs = dto.durationMs,
            isPlayable = dto.isPlayable ?: true,
            type = dto.type,
            uri = dto.uri,
            artists = dto.artists.map { artistDto ->
                Artist(
                    id = artistDto.id,
                    name = artistDto.name
                )
            },
            album = AlbumSimplified(
                name = dto.album.name,
                images = dto.album.images.map { img ->
                    Image(height = img.height ?: 0, width = img.width ?: 0, url = img.url)
                },
            ),
            imageUrl = dto.album.images.firstOrNull()?.url
        )
    }
}
