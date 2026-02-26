package org.ilerna.song_swipe_frontend.presentation.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.core.state.UiState
import org.ilerna.song_swipe_frontend.domain.usecase.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.mapper.toPlaylistTrackUi


/**
 * ViewModel that loads the tracks/items from the user's default playlist.
 */
class PlaylistItemsViewModel(
    private val getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase? = null,
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase? = null
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<PlaylistTrackUi>>>(UiState.Idle)
    val state: StateFlow<UiState<List<PlaylistTrackUi>>> = _state.asStateFlow()

    private var cachedPlaylistId: String? = null

    fun load(supabaseUserId: String, spotifyUserId: String) {
        val defaultUseCase = getOrCreateDefaultPlaylistUseCase ?: run {
            _state.value = UiState.Error("Use case not provided: GetOrCreateDefaultPlaylistUseCase")
            return
        }
        val trackUseCase = getPlaylistTracksUseCase ?: run {
            _state.value = UiState.Error("Use case not provided: GetPlaylistTracks")
            return
        }
        if (supabaseUserId.isBlank() || spotifyUserId.isBlank()) {
            _state.value = UiState.Error("Missing user IDs")
            return
        }

        viewModelScope.launch {
            _state.value = UiState.Loading

            /**
             *  1. Get/Create default playlist
             */
            val playlistId = when (val p = defaultUseCase(supabaseUserId, spotifyUserId)) {
                is NetworkResult.Success -> {
                    val id = p.data.id
                    cachedPlaylistId = id
                    id
                }

                is NetworkResult.Error -> {
                    _state.value = UiState.Error(p.message)
                    return@launch
                }

                is NetworkResult.Loading -> {
                    _state.value = UiState.Loading
                    return@launch
                }
            }

            if (playlistId.isBlank()) {
                _state.value = UiState.Error("Default playlist ID is empty")
                return@launch
            }

            /**
             * 2. Get tracks from playlist
             */
            when (val t = trackUseCase(playlistId)) {
                is NetworkResult.Success -> {
                    val tracks = (t.data ?: emptyList()).map { it.toPlaylistTrackUi() }
                    _state.value = UiState.Success(tracks)
                }

                is NetworkResult.Error -> _state.value = UiState.Error(t.message)
                is NetworkResult.Loading -> _state.value = UiState.Loading
            }
        }
    }

    fun retry(supabaseUserId: String, spotifyUserId: String) {
        load(supabaseUserId, spotifyUserId)
    }
}

/**
 * UI Model (Null-safe)
 */
data class PlaylistTrackUi(
    val id: String, val title: String, val artists: String, val imageUrl: String?
)