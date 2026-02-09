package org.ilerna.song_swipe_frontend.data.repository.mapper


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
