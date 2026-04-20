package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.RemoveItemFromPlaylistUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.mapper.toPlaylistTrackUi

/**
 * PlaylistViewModel
 * Drives PlaylistDetailsScreen — loads tracks for a specific playlist and
 * handles track deletion via Spotify API.
 */
class PlaylistViewModel(
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    private val removeItemFromPlaylistUseCase: RemoveItemFromPlaylistUseCase
) : ViewModel() {

    private val _tracksState = MutableStateFlow<UiState<List<PlaylistTrackUi>>>(UiState.Idle)
    val tracksState: StateFlow<UiState<List<PlaylistTrackUi>>> = _tracksState.asStateFlow()

    private val _trackToDelete = MutableStateFlow<PlaylistTrackUi?>(null)
    val trackToDelete: StateFlow<PlaylistTrackUi?> = _trackToDelete.asStateFlow()

    fun loadTracks(playlistId: String) {
        if (playlistId.isBlank()) {
            _tracksState.value = UiState.Error("Missing playlist ID")
            return
        }

        viewModelScope.launch {
            _tracksState.value = UiState.Loading

            when (val t = getPlaylistTracksUseCase(listOf(playlistId))) {
                is NetworkResult.Success -> {
                    val tracks = t.data.map { it.toPlaylistTrackUi() }
                    _tracksState.value = UiState.Success(tracks)
                }
                is NetworkResult.Error -> _tracksState.value = UiState.Error(t.message)
                is NetworkResult.Loading -> _tracksState.value = UiState.Loading
            }
        }
    }

    fun retry(playlistId: String) = loadTracks(playlistId)

    fun requestDeleteTrack(track: PlaylistTrackUi) {
        _trackToDelete.value = track
    }

    fun cancelDeleteTrack() {
        _trackToDelete.value = null
    }

    fun confirmDeleteTrack(playlistId: String) {
        val track = _trackToDelete.value ?: return

        viewModelScope.launch {
            _trackToDelete.value = null

            when (removeItemFromPlaylistUseCase(playlistId = playlistId, trackId = track.id)) {
                is NetworkResult.Success -> {
                    val current = (_tracksState.value as? UiState.Success)?.data ?: return@launch
                    _tracksState.value = UiState.Success(current.filter { it.id != track.id })
                }
                is NetworkResult.Error -> {
                    loadTracks(playlistId)
                }
                is NetworkResult.Loading -> { /* no-op */ }
            }
        }
    }
}
