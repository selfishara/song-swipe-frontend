package org.ilerna.song_swipe_frontend.presentation.screen.playlist.mapper

import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.PlaylistTrackUi

fun Track.toPlaylistTrackUi(): PlaylistTrackUi {
    val artistsText = artists
        .mapNotNull { it.name.takeIf { name -> name.isNotBlank() } }
        .joinToString(", ")
        .ifBlank { "Unknown artist" }

    return PlaylistTrackUi(
        id = id,
        title = name.ifBlank { "No title" },
        artists = artistsText,
        imageUrl = imageUrl
    )
}