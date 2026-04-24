package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.CreatePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase

/**
 * Drives the PlaylistsScreen — the list of every playlist the user owns or follows.
 * Tracks the active playlist (persisted in DataStore) and exposes actions to change it
 * or create a new playlist on Spotify.
 */
class PlaylistListViewModel(
    private val getUserPlaylistsUseCase: GetUserPlaylistsUseCase,
    private val getActivePlaylistUseCase: GetActivePlaylistUseCase,
    private val setActivePlaylistUseCase: SetActivePlaylistUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val spotifyUserId: String
) : ViewModel() {

    private val _playlistsState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Idle)
    val playlistsState: StateFlow<UiState<List<Playlist>>> = _playlistsState.asStateFlow()

    val activePlaylistId: StateFlow<String?> = getActivePlaylistUseCase.id()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _createError = MutableStateFlow<String?>(null)
    val createError: StateFlow<String?> = _createError.asStateFlow()

    fun loadPlaylists() {
        viewModelScope.launch {
            _playlistsState.value = UiState.Loading
            when (val result = getUserPlaylistsUseCase()) {
                is NetworkResult.Success -> {
                    _playlistsState.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _playlistsState.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> _playlistsState.value = UiState.Loading
            }
        }
    }

    fun setActivePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            setActivePlaylistUseCase(
                playlistId = playlist.id,
                playlistName = playlist.name
            )
        }
    }

    fun createPlaylist(name: String, description: String?, isPublic: Boolean) {
        if (name.isBlank() || spotifyUserId.isBlank()) {
            _createError.value = "Playlist name is required"
            return
        }

        viewModelScope.launch {
            when (val result = createPlaylistUseCase(
                userId = spotifyUserId,
                name = name.trim(),
                description = description?.takeIf { it.isNotBlank() },
                isPublic = isPublic
            )) {
                is NetworkResult.Success -> {
                    _createError.value = null
                    loadPlaylists()
                }
                is NetworkResult.Error -> {
                    _createError.value = result.message
                }
                is NetworkResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearCreateError() {
        _createError.value = null
    }
}
