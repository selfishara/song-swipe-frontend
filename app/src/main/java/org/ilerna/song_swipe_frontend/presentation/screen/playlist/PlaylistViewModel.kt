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
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.RemoveItemFromDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.playlist.mapper.toPlaylistTrackUi

/**
 * PlaylistViewModel
 * Handles "liked tracks" logic (default playlist tracks).
 */
class PlaylistViewModel(
    private val getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase? = null,
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase? = null,
    private val removeItemFromDefaultPlaylistUseCase: RemoveItemFromDefaultPlaylistUseCase? = null
) : ViewModel() {

    // Liked tracks (default playlist)
    private val _likedTracksState = MutableStateFlow<UiState<List<PlaylistTrackUi>>>(UiState.Idle)
    val likedTracksState: StateFlow<UiState<List<PlaylistTrackUi>>> =
        _likedTracksState.asStateFlow()

    // Track pending deletion (for confirmation dialog)
    private val _trackToDelete = MutableStateFlow<PlaylistTrackUi?>(null)
    val trackToDelete: StateFlow<PlaylistTrackUi?> = _trackToDelete.asStateFlow()

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
            when (val t = trackUseCase(listOf(playlistId))) {
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

    /**
     * Requests deletion of a track — shows confirmation dialog.
     */
    fun requestDeleteTrack(track: PlaylistTrackUi) {
        _trackToDelete.value = track
    }

    /**
     * Cancels the pending track deletion.
     */
    fun cancelDeleteTrack() {
        _trackToDelete.value = null
    }

    /**
     * Confirms deletion: removes the track from the default playlist via Spotify API
     * and updates the local list optimistically.
     */
    fun confirmDeleteTrack(supabaseUserId: String, spotifyUserId: String) {
        val track = _trackToDelete.value ?: return
        val useCase = removeItemFromDefaultPlaylistUseCase ?: return

        viewModelScope.launch {
            _trackToDelete.value = null

            when (useCase(supabaseUserId, spotifyUserId, track.id)) {
                is NetworkResult.Success -> {
                    // Remove track from local list
                    val current = (_likedTracksState.value as? UiState.Success)?.data ?: return@launch
                    _likedTracksState.value = UiState.Success(current.filter { it.id != track.id })
                }
                is NetworkResult.Error -> {
                    // Reload to stay in sync
                    loadLikedTracks(supabaseUserId, spotifyUserId)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}