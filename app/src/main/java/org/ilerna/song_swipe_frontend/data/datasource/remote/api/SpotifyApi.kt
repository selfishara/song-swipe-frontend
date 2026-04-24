package org.ilerna.song_swipe_frontend.data.datasource.remote.api

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyAddItemsRequestDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyCreatePlaylistRequestDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyRemoveItemsRequestDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifySnapshotResponseDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTracksResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserPlaylistsResponseDto
import org.ilerna.song_swipe_frontend.data.remote.dto.response.SpotifyCreatePlaylistResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
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
     * Get a list of the playlists owned or followed by the current Spotify user
     * Requires: playlist-read-private, playlist-read-collaborative scopes
     *
     * @param limit Maximum number of playlists to return (max 50)
     * @param offset Index of the first playlist to return
     * @return Response containing paginated playlist data
     */
    @GET("v1/me/playlists")
    suspend fun getCurrentUserPlaylists(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<SpotifyUserPlaylistsResponseDto>

    @GET("v1/playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracksPaged(
        @Path("playlist_id") playlistId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("market") market: String? = null
    ): Response<SpotifyTracksResponse>

    /**
     * Create a new playlist for a user
     *
     * @param userId The Spotify ID of the user
     * @param request The request body containing playlist details
     * @return SpotifyCreatePlaylistResponseDto containing the created playlist details
     */
    @POST("v1/users/{user_id}/playlists")
    suspend fun createPlaylist(
        @Path("user_id") userId: String,
        @Body request: SpotifyCreatePlaylistRequestDto
    ): SpotifyCreatePlaylistResponseDto

    /**
     * Add items (tracks) to a playlist
     *
     * @param playlistId The Spotify ID of the playlist
     * @param body The request body containing the URIs of the tracks to add
     * @return SpotifySnapshotResponseDto containing the snapshot ID of the playlist after modification
     */
    @POST("v1/playlists/{playlist_id}/tracks")
    suspend fun addItemsToPlaylist(
        @Path("playlist_id") playlistId: String,
        @Body body: SpotifyAddItemsRequestDto
    ): Response<SpotifySnapshotResponseDto>

    /**
     * Remove items (tracks) from a playlist.
     * Uses @HTTP because @DELETE does not support a request body.
     *
     * @param playlistId The Spotify ID of the playlist
     * @param body The request body containing the track URIs to remove
     * @return SpotifySnapshotResponseDto containing the snapshot ID after modification
     */
    @HTTP(method = "DELETE", path = "v1/playlists/{playlist_id}/tracks", hasBody = true)
    suspend fun removeItemsFromPlaylist(
        @Path("playlist_id") playlistId: String,
        @Body body: SpotifyRemoveItemsRequestDto
    ): Response<SpotifySnapshotResponseDto>
}