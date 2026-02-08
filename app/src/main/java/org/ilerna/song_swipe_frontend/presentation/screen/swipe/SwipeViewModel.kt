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

/**
 * ViewModel handling swipe interactions logic.
 *
 * - RIGHT swipe: saves song (mock)
 * - LEFT swipe: discards song
 *
 * Real persistence/navigation will be implemented in future sprints.
 */
class SwipeViewModel : ViewModel() {

    // Lista fake para este sprint (luego se cambia por data/remote)
    var songs by mutableStateOf(
        listOf(
            SongUiModel("1", "Blinding Lights", "The Weeknd", null),
            SongUiModel("2", "One More Time", "Daft Punk", null),
            SongUiModel("3", "Bad Guy", "Billie Eilish", null),
            SongUiModel("4", "Levitating", "Dua Lipa", null),
            SongUiModel("5", "Watermelon Sugar", "Harry Styles", null),
            SongUiModel("6", "Peaches", "Justin Bieber", null),
            SongUiModel("7", "Save Your Tears", "The Weeknd", null),
            SongUiModel("8", "Stay", "The Kid LAROI & Justin Bieber", null),
            SongUiModel("9", "good 4 u", "Olivia Rodrigo", null),
            SongUiModel("10", "Amanece", "Anuel AA", null),
            SongUiModel("11", "Industry Baby", "Lil Nas X & Jack Harlow", null)
        )
    )
        private set

    var currentIndex by mutableIntStateOf(0)
        private set

    val likedSongs = mutableStateListOf<SongUiModel>()

    // track songs played in this session ----
    var playedSongsCount by mutableIntStateOf(0)
        private set

    // ensure navigation to Playlist happens only once per cycle
    var hasNavigatedToPlaylist by mutableStateOf(false)
        private set

    // Callback to trigger navigation
    var onTenSongsPlayed: (() -> Unit)? = null
    fun currentSongOrNull(): SongUiModel? = songs.getOrNull(currentIndex)

    fun onSwipe(direction: SwipeDirection) {
        if (hasNavigatedToPlaylist) return
        val song = currentSongOrNull() ?: return

        playedSongsCount += 1

        when (direction) {
            SwipeDirection.LEFT -> {
                Log.d("Swipe", "Discarded: ${song.id}")
                next()
            }
            SwipeDirection.RIGHT -> {
                save(song)
                next()
            }
        }

        // Navigate automatically after 10 songs
        if (playedSongsCount >= 10 && !hasNavigatedToPlaylist) {
            hasNavigatedToPlaylist = true
            onTenSongsPlayed?.invoke()
        }
    }
    private fun save(song: SongUiModel) {
        if (likedSongs.none { it.id == song.id }) likedSongs.add(song)
        Log.d("Swipe", "Saved: ${song.id}")
    }

    private fun next() {
        currentIndex += 1
    }

    // reset session counter and navigation flag
    fun resetSession() {
        playedSongsCount = 0
        hasNavigatedToPlaylist = false
        currentIndex = 0
    }
}