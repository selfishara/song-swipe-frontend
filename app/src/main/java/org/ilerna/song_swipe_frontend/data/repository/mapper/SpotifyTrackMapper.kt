package org.ilerna.song_swipe_frontend.data.repository.mapper

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.*
import org.ilerna.song_swipe_frontend.domain.model.*

object SpotifyTrackMapper {

    /**
     * Maps a SpotifyTrackDto to a Track domain model
     */
    fun toDomain(dto: SpotifyTrackDto): Track {
        return Track(
            id = dto.id,
            name = dto.name,
            artists = dto.artists.map { toDomain(it) },
            album = toDomain(dto.album),
            duration_ms = dto.durationMs,
            preview_url = dto.previewUrl ?: "",
            is_playable = dto.isPlayable ?: true,
            type = dto.type ?: "track",
            uri = dto.uri ?: "spotify:track:${dto.id}"
        )
    }

    /**
     * Maps a SpotifyArtistDto to an Artist domain model
     */
    fun toDomain(dto: SpotifyArtistDto): Artist {
        return Artist(
            id = dto.id,
            name = dto.name
        )
    }

    /**
     * Maps a SpotifyAlbumDto to an Album domain model
     */
    fun toDomain(dto: SpotifyAlbumDto): Album {
        return Album(
            album_type = dto.albumType ?: "",
            artists = dto.artists?.map { toDomain(it) } ?: emptyList(),
            available_markets = dto.availableMarkets ?: emptyList(),
            href = dto.href ?: "",
            id = dto.id ?: "",
            images = dto.images?.map { toDomain(it) } ?: emptyList(),
            name = dto.name,
            release_date = dto.releaseDate ?: "",
            release_date_precision = dto.releaseDatePrecision ?: "",
            total_tracks = dto.totalTracks ?: 0,
            type = dto.type ?: "album",
            uri = dto.uri ?: ""
        )
    }

    /**
     * Maps a SpotifyImageDto to an Image domain model
     */
    fun toDomain(dto: SpotifyImageDto): Image {
        return Image(
            url = dto.url,
            height = dto.height ?: 0,
            width = dto.width ?: 0
        )
    }
}