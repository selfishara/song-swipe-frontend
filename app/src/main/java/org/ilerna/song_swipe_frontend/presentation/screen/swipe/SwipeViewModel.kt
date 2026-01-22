package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel

enum class SwipeDirection { LEFT, RIGHT }

class SwipeViewModel : ViewModel() {

    // Lista fake para este sprint (luego se cambia por data/remote)
    var songs by mutableStateOf(
        listOf(
            SongUiModel("1", "Blinding Lights", "The Weeknd", null),
            SongUiModel("2", "One More Time", "Daft Punk", null),
            SongUiModel("3", "Bad Guy", "Billie Eilish", null)
        )
    )
        private set

    var currentIndex by mutableIntStateOf(0)
        private set

    val likedSongs = mutableStateListOf<SongUiModel>()

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
}