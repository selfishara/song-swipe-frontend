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
                "0fpooyN1o9Nc2wJO0zNBea",
                "2xjYnvLKZgxjIXqHXYV0Zs",
                "0D3OBV654y6cJRwg9bztkk",
                "5tkmp4FQqwVfCSia1pNKVu",
                "37i9dQZF1EIdxwrH5wQhtc",
                "3tRhisNDv5YZXPQltBbJNc",
                "5h1oEk4W9KVMHkOd8WWWlC",
                "5OuwluOQwdzeTMc3ZqTHcI",
                "1IGF7xoNwjg3FnNZTmT5XU",
                "39nFCFRtOYIRkfx0hggGHa"


            ),
            "Hip Hop" to listOf(
                "7gxKeEYlRRf16vdpqVQwmQ",
                "37i9dQZF1EQnqst5TRi17F",
                "3RcRK9HGTAm9eLW1LepWKZ",
                "0dMexqq0XIWS3QJ74z3ZhD",
                "6dPoZmfSRyD4yytxIVlTyO",
                "2faxw54KX2Y6XvPHkSUcWo",
                "2ZmBEJRMH8rgtVB1GonReM",
                "37i9dQZF1DX76t638V6CA8",
                "37i9dQZF1DX9oh43oAzkyx",
                "6YOTa2JPtWJDPjX3xJr4ts"
            ),
            "Pop" to listOf(
                "37i9dQZF1DX1ngEVM0lKrb",
                "741f3cdUlLHg7t4Oosy4fq",
                "0ehAPnl6xtTH4fQKFNcFf8",
                "2UZk7JjJnbTut1w8fqs3JL",
                "3JeuDb2LgAaCOHkTsXlOF9",
                "1lroF7DAwmvWcjxOnysMg4",
                "3u1d7CUhrW0E9RFzs8EMmV",
                "32rEBAnM7SuIMmjovIrDpS",
                "09jSNJqrXOgXnIMRaBEMvC",
                "3SIVHRHoHESDTB5N8dCjul"
            ),
            "Metal" to listOf(
                "1GXRoQWlxTNQiMNkOe7RqA",
                "2KB2kUNfUtujzoYvW0lvyN",
                "37i9dQZF1EQpgT26jgbgRI",
                "37i9dQZF1EIgsBucdAI1u5",
                "27gN69ebwiJRtXEboL12Ih",
                "3MgDK9ctKTarGZ9n70Q2jZ",
                "0UVHqzfeBfsqdjnAxklasi",
                "14tJZlaI9IACMzU1sdKzCK",
                "0mAS2nwUZVBbDof6PH6g7d",
                "1GKqC6Rq1O3o97UwWMuiq6"
            ),
            "Reggaeton" to listOf(
                "29jkNw8QoKFHob2Tc7Gdci",
                "5QZa4A1P6rtiq11OrZUbdq",
                "1WLBRi9Y2s9cbRN6IvFg3m",
                "03sDEv7FN58Mb9CJOs1Tgn",
                "37i9dQZF1DX1HCSfq0nSal",
                "0anvI2Ia2qzCAPO6l0ylOM",
                "6DhSVIZ2A2Z8wPJzkGNPWd",
                "1YCtESfhweTEN1Kc9J3aOy",
                "52B2583XuIuFQ9zyNvyQrk",
                "5Ss1hTuFojiEwyFO0HTAim"
            )
        )
    }
}