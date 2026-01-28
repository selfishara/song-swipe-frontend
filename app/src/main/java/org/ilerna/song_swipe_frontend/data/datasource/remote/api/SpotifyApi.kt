package org.ilerna.song_swipe_frontend.data.datasource.remote.api

import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyTracksResponse
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyUserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

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

    @GET("v1/playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(@Path("playlist_id") playlistId: String): Response<SpotifyTracksResponse>
}