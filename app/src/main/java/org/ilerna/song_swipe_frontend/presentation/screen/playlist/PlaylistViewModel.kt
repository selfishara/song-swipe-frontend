package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetPlaylistsByGenreUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.mapper.toPlaylistTrackUi

/**
 * PlaylistViewModel
 * - Keeps the original "playlists by genre" logic
 * - Adds "liked tracks" logic (default playlist tracks)
 *
 * This way we avoid having 2 ViewModels for the same feature.
 */
class PlaylistViewModel(
    private val getPlaylistsByGenreUseCase: GetPlaylistsByGenreUseCase? = null,
    private val getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase? = null,
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase? = null
) : ViewModel() {

    // Existing feature: playlists by genre
    private val _playlistsState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Idle)
    val playlistsState: StateFlow<UiState<List<Playlist>>> = _playlistsState.asStateFlow()

    // New feature: liked tracks (default playlist)
    private val _likedTracksState = MutableStateFlow<UiState<List<PlaylistTrackUi>>>(UiState.Idle)
    val likedTracksState: StateFlow<UiState<List<PlaylistTrackUi>>> =
        _likedTracksState.asStateFlow()

    /**
     * Loads playlists for the given genre.
     */
    fun loadByGenre(genre: String) {
        val useCase = getPlaylistsByGenreUseCase ?: run {
            _playlistsState.value =
                UiState.Error("Use case not provided: GetPlaylistsByGenreUseCase")
            return
        }

        viewModelScope.launch {
            _playlistsState.value = UiState.Loading
            when (val result = useCase(genre)) {
                is NetworkResult.Success -> _playlistsState.value = UiState.Success(result.data)
                is NetworkResult.Error -> _playlistsState.value = UiState.Error(result.message)
                is NetworkResult.Loading -> _playlistsState.value = UiState.Loading
            }
        }
    }

    /**
     * Loads tracks from the user's default "liked" playlist.
     */
    fun loadLikedTracks(supabaseUserId: String, spotifyUserId: String) {
        val defaultUseCase = getOrCreateDefaultPlaylistUseCase ?: run {
            _likedTracksState.value =
                UiState.Error("Use case not provided: GetOrCreateDefaultPlaylistUseCase")
            return
        }
        val trackUseCase = getPlaylistTracksUseCase ?: run {
            _likedTracksState.value =
                UiState.Error("Use case not provided: GetPlaylistTracksUseCase")
            return
        }

        if (supabaseUserId.isBlank() || spotifyUserId.isBlank()) {
            _likedTracksState.value = UiState.Error("Missing user IDs")
            return
        }

        viewModelScope.launch {
            _likedTracksState.value = UiState.Loading

            // 1) Ensure default playlist exists
            val playlistId = when (val p = defaultUseCase(supabaseUserId, spotifyUserId)) {
                is NetworkResult.Success -> p.data.id
                is NetworkResult.Error -> {
                    _likedTracksState.value = UiState.Error(p.message)
                    return@launch
                }

                is NetworkResult.Loading -> {
                    _likedTracksState.value = UiState.Loading
                    return@launch
                }
            }

            if (playlistId.isBlank()) {
                _likedTracksState.value = UiState.Error("Default playlist ID is empty")
                return@launch
            }

            // 2) Fetch tracks from playlist
            when (val t = trackUseCase(playlistId)) {
                is NetworkResult.Success -> {
                    val tracks = (t.data ?: emptyList()).map { it.toPlaylistTrackUi() }
                    _likedTracksState.value = UiState.Success(tracks)
                }

                is NetworkResult.Error -> _likedTracksState.value = UiState.Error(t.message)
                is NetworkResult.Loading -> _likedTracksState.value = UiState.Loading
            }
        }
    }

    fun retryLikedTracks(supabaseUserId: String, spotifyUserId: String) {
        loadLikedTracks(supabaseUserId, spotifyUserId)
    }
}