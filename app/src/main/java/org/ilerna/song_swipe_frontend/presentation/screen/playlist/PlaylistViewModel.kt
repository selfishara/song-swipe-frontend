package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetPlaylistsByGenreUseCase

/**
 * ViewModel for fetching playlists by genre.
 * Uses a use case to retrieve clean domain Playlist models.
 */
class PlaylistViewModel(
    private val getPlaylistsByGenreUseCase: GetPlaylistsByGenreUseCase? = null
) : ViewModel() {

    private val _state = MutableStateFlow<PlaylistsState>(PlaylistsState.Idle)
    val state: StateFlow<PlaylistsState> = _state.asStateFlow()

    /**
     * Loads playlists for the given genre using the use case.
     */
    fun load(genre: String) {
        val useCase = getPlaylistsByGenreUseCase ?: run {
            _state.value = PlaylistsState.Error("Use case not provided")
            return
        }

        viewModelScope.launch {
            _state.value = PlaylistsState.Loading

            when (val result = useCase(genre)) {
                is NetworkResult.Success -> {
                    _state.value = PlaylistsState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _state.value = PlaylistsState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _state.value = PlaylistsState.Loading
                }
            }
        }
    }
}

/**
 * UI state for playlists screen.
 */
sealed class PlaylistsState {
    data object Idle : PlaylistsState()
    data object Loading : PlaylistsState()
    data class Success(val playlists: List<Playlist>) : PlaylistsState()
    data class Error(val message: String) : PlaylistsState()
}