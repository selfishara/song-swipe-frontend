package org.ilerna.song_swipe_frontend.data.datasource.remote.api

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyCategoriesResponseDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyCategoryPlaylistsResponseDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.PlaylistTracksResponseDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Spotify Web API interface for Retrofit
 * Base URL: https://api.spotify.com/
 */
interface SpotifyApi {

    /**
     * Get detailed profile information about the current user
     * Requires: user-read-email, user-read-private scopes
     *
     * @return Response containing the user's Spotify profile data
     */
    @GET("v1/me")
    suspend fun getCurrentUserProfile(): Response<SpotifyUserDto>

    /**
     * Get Spotify browse categories (used as genres)
     */
    @GET("v1/browse/categories")
    suspend fun getCategories(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<SpotifyCategoriesResponseDto>

    /**
     * Get playlists for a specific Spotify category (genre)
     */
    @GET("v1/browse/categories/{categoryId}/playlists")
    suspend fun getCategoryPlaylists(
        @Path("categoryId") categoryId: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<SpotifyCategoryPlaylistsResponseDto>

    /**
     * Get tracks of a specific playlist
     *
     * @param playlistId The Spotify ID of the playlist
     * @return PlaylistTracksResponseDto containing the tracks in the playlist
     */
    @GET("v1/playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(
        @Path("playlist_id") playlistId: String
    ): PlaylistTracksResponseDto
}
