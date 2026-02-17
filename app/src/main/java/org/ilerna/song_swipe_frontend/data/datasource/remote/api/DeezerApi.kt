package org.ilerna.song_swipe_frontend.data.datasource.remote.api

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.DeezerSearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Deezer Web API interface for Retrofit.
 * Base URL: https://api.deezer.com/
 *
 * Used exclusively to obtain 30-second track preview URLs,
 * since Spotify deprecated their preview_url field.
 *
 * This API is public and does NOT require authentication.
 */
interface DeezerApi {

    /**
     * Search for tracks on Deezer.
     *
     * @param query Search query string. Supports advanced syntax:
     *              - track:"song name" artist:"artist name"
     *              - or simple text: "song name artist name"
     * @param limit Maximum number of results (default 1, we only need the best match)
     * @return Response containing search results with preview URLs
     */
    @GET("search")
    suspend fun searchTrack(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1
    ): Response<DeezerSearchResponseDto>
}
