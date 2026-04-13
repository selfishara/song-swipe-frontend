package org.ilerna.song_swipe_frontend.data.provider

/**
 * Provides the curated Spotify playlist IDs available for each genre.
 *
 * Each genre can have any number of playlists.
 * To add a new genre: add a new entry to [genrePlaylistMap] with at least one playlist ID.
 */
class GenrePlaylistProvider {

    fun getGenres(): Set<String> = genrePlaylistMap.keys

    fun getPlaylistIdsForGenre(genre: String): List<String> =
        genrePlaylistMap[genre].orEmpty()

    fun getPrimaryPlaylistIdForGenre(genre: String): String? =
        getPlaylistIdsForGenre(genre).firstOrNull()

    companion object {
        const val DEFAULT_SET_SIZE: Int = 50

        private val genrePlaylistMap: Map<String, List<String>> = mapOf(
            "Electronic" to listOf(
                "0fpooyN1o9Nc2wJO0zNBea"
            ),
            "Hip Hop" to listOf(
                "7gxKeEYlRRf16vdpqVQwmQ"
            ),
            "Pop" to listOf(
                "7w0Fy9FiPOKFTYkZDPiY6R"
            ),
            "Metal" to listOf(
                "1GXRoQWlxTNQiMNkOe7RqA"
            ),
            "Reggaeton" to listOf(
                "7Dj5Oo9FJYVesuPVIkRQix"
            )
        )
    }
}