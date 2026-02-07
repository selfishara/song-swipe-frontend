package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel
enum class SwipeDirection { LEFT, RIGHT }

/**
 * ViewModel handling swipe interactions logic.
 *
 * - RIGHT swipe: saves song (mock)
 * - LEFT swipe: discards song
 *
 * Real persistence/navigation will be implemented in future sprints.
 */
private const val DEFAULT_PLAYLIST_ID = "37i9dQZF1DXcBWIGoYBM5M"

class SwipeViewModel (private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase): ViewModel() {


    // Lista fake para este sprint (luego se cambia por data/remote)
   /**
    * Lo comento por si lo mio no funciona xd
    *
    var songs by mutableStateOf(
        listOf(
            SongUiModel("1", "Blinding Lights", "The Weeknd", null),
            SongUiModel("2", "One More Time", "Daft Punk", null),
            SongUiModel("3", "Bad Guy", "Billie Eilish", null)
        )
    )
        private set
**/
   var songs by mutableStateOf<List<SongUiModel>>(emptyList())
       private set
    var currentIndex by mutableIntStateOf(0)
        private set

    val likedSongs = mutableStateListOf<SongUiModel>()
    init {
        loadSongs(DEFAULT_PLAYLIST_ID)
    }

    fun currentSongOrNull(): SongUiModel? = songs.getOrNull(currentIndex)

    fun onSwipe(direction: SwipeDirection) {
        val song = currentSongOrNull() ?: return

        when (direction) {
            SwipeDirection.LEFT -> {
                // No guarda
                Log.d("Swipe", "Discarded: ${song.id}")
                next()
            }

            SwipeDirection.RIGHT -> {
                // Guarda
                save(song)
                next()
            }
        }
    }

    private fun save(song: SongUiModel) {
        if (likedSongs.none { it.id == song.id }) likedSongs.add(song)
        Log.d("Swipe", "Saved: ${song.id}")
    }

    private fun next() {
        currentIndex += 1
    }


    // --- LOGICA DE TRACKS --- //

    //TODO: Implement UiState to handle loading and error states in the Swipe UI.
    // Currently, loading and error are only logged and not exposed to the UI.
    // This can be refactored to use StateFlow<UiState<List<SongUiModel>>> once
    // navigation and UI states are properly defined.
    fun loadSongs(playlistId: String) {
        viewModelScope.launch {
            Log.d("SwipeViewModel", "Cargando canciones reales...")
            when (val result = getPlaylistTracksUseCase(playlistId)) {

                is NetworkResult.Success -> {
                    songs = result.data.map { track ->
                        SongUiModel(
                            id = track.id,
                            title = track.name,
                            artist = track.artists.joinToString(", ") { it.name },
                            imageUrl = track.imageUrl
                        )
                    }
                    currentIndex = 0
                    Log.d("SwipeViewModel", "Se cargaron ${songs.size} canciones")
                }

                is NetworkResult.Error -> {
                    Log.e(
                        "SwipeViewModel",
                        "Error cargando canciones: ${result.message}"
                    )
                }

                is NetworkResult.Loading -> {
                    Log.d("SwipeViewModel", "Cargando...")
                }
        }
    }
}}