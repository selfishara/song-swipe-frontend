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
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetPlaylistsByGenreUseCase

/**
 * ViewModel for fetching playlists by genre.
 * Uses a use case to retrieve clean domain Playlist models.
 */
class PlaylistViewModel(
    private val getPlaylistsByGenreUseCase: GetPlaylistsByGenreUseCase? = null
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Playlist>>>(UiState.Idle)
    val state: StateFlow<UiState<List<Playlist>>> = _state.asStateFlow()

    /**
     * Loads playlists for the given genre using the use case.
     */
    fun load(genre: String) {
        val useCase = getPlaylistsByGenreUseCase ?: run {
            _state.value = UiState.Error("Use case not provided")
            return
        }

        viewModelScope.launch {
            _state.value = UiState.Loading

            when (val result = useCase(genre)) {
                is NetworkResult.Success -> {
                    _state.value = UiState.Success(result.data)
                }
                is NetworkResult.Error -> {
                    _state.value = UiState.Error(result.message)
                }
                is NetworkResult.Loading -> {
                    _state.value = UiState.Loading
                }
            }
        }
    }
}