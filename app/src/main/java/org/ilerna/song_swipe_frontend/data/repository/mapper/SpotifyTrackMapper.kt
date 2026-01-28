package org.ilerna.song_swipe_frontend.data.repository.mapper

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTrackDto
import org.ilerna.song_swipe_frontend.domain.model.Album
import org.ilerna.song_swipe_frontend.domain.model.Artist
import org.ilerna.song_swipe_frontend.domain.model.Image
import org.ilerna.song_swipe_frontend.domain.model.Track

fun SpotifyTrackDto.toDomain(): Track {
    return Track(
        id = this.id,
        name = this.name,
        previewUrl = this.previewUrl,
        durationMs = this.durationMs,
        isPlayable = this.isPlayable ?: true,
        type = this.type,
        uri = this.uri,
        artists = this.artists.map { artistDto ->
            Artist(
                id = artistDto.id,
                name = artistDto.name
            )
        },
        album = Album(
            id = "", // No se proporciona en el DTO
            name = this.album.name,
            images = this.album.images.map { img ->
                Image(height = img.height ?: 0, width = img.width ?: 0, url = img.url)
            },
            //Pongo esto porque sino no funciona el mapper
            album_type = "",
            artists = emptyList(),
            available_markets = emptyList(),
            href = "",
            release_date = "",
            release_date_precision = "",
            total_tracks = 0,
            type = "album",
            uri = ""
        ),
        imageUrl = this.album.images.firstOrNull()?.url
    )
}
