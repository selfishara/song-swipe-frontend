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
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
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
private const val SOURCE_PLAYLIST_ID = "1z6ObE7LuXgoSgRIoruyMr"

class SwipeViewModel (
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    private val getTrackPreviewUseCase: GetTrackPreviewUseCase,
    private val getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase,
    private val supabaseUserId: String,
    private val spotifyUserId: String
): ViewModel() {

   var songs by mutableStateOf<List<SongUiModel>>(emptyList())
       private set
    var currentIndex by mutableIntStateOf(0)
        private set

    val likedSongs = mutableStateListOf<SongUiModel>()
    init {
        loadSongs(SOURCE_PLAYLIST_ID)
        initializeDefaultPlaylist()
    }

    /**
     * Ensures the default playlist exists (checks Supabase, creates on Spotify if needed).
     * Runs in the background on ViewModel init.
     */
    private fun initializeDefaultPlaylist() {
        viewModelScope.launch {
            try {
                when (val result = getOrCreateDefaultPlaylistUseCase(supabaseUserId, spotifyUserId)) {
                    is NetworkResult.Success -> {
                        Log.d("SwipeViewModel", "Default playlist ready: ${result.data.name} (${result.data.id})")
                    }
                    is NetworkResult.Error -> {
                        Log.e("SwipeViewModel", "Error initializing default playlist: ${result.message}")
                    }
                    is NetworkResult.Loading -> { /* no-op */ }
                }
            } catch (e: Exception) {
                Log.e("SwipeViewModel", "Error initializing default playlist: ${e.message}", e)
            }
        }
    }

    fun currentSongOrNull(): SongUiModel? = songs.getOrNull(currentIndex)

    fun onSwipe(direction: SwipeDirection) {
        val song = currentSongOrNull() ?: return

        when (direction) {
            SwipeDirection.LEFT -> {
                // Skip song
                Log.d("Swipe", "Discarded: ${song.id}")
                next()
            }

            SwipeDirection.RIGHT -> {
                // Save song
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


    // --- Tracks logic --- //

    // TODO: Implement UiState to handle loading and error states in the Swipe UI.
    // Currently, loading and error are only logged and not exposed to the UI.
    // This can be refactored to use StateFlow<UiState<List<SongUiModel>>> once
    // navigation and UI states are properly defined.
    fun loadSongs(playlistId: String) {
        viewModelScope.launch {
            Log.d("SwipeViewModel", "Cargando canciones reales...")
            when (val result = getPlaylistTracksUseCase(playlistId)) {

                is NetworkResult.Success -> {
                    // First map tracks to UI models without preview URLs
                    val initialSongs = result.data.map { track ->
                        SongUiModel(
                            id = track.id,
                            title = track.name,
                            artist = track.artists.joinToString(", ") { it.name },
                            imageUrl = track.imageUrl,
                            previewUrl = track.previewUrl // May be null from Spotify
                        )
                    }
                    songs = initialSongs
                    currentIndex = 0
                    Log.d("SwipeViewModel", "Se cargaron ${songs.size} canciones")

                    // Enrich songs with Deezer preview URLs in the background
                    enrichWithDeezerPreviews(initialSongs)
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
    }

    /**
     * Enriches songs that lack a preview URL by fetching from Deezer.
     * Updates the songs list progressively as previews are found.
     */
    private suspend fun enrichWithDeezerPreviews(songList: List<SongUiModel>) {
        val enrichedSongs = songList.toMutableList()
        var updated = false

        for ((index, song) in songList.withIndex()) {
            // Skip songs that already have a preview URL from Spotify
            // Currently unreachable due to Spotify's deprecation, but this check allows for future flexibility
            if (song.previewUrl != null) continue

            try {
                val previewResult = getTrackPreviewUseCase(
                    trackName = song.title,
                    artistName = song.artist.split(",").first().trim()
                )

                if (previewResult is NetworkResult.Success && previewResult.data != null) {
                    enrichedSongs[index] = song.copy(previewUrl = previewResult.data)
                    updated = true
                    Log.d("SwipeViewModel", "Deezer preview found for: ${song.title}")
                } else {
                    Log.d("SwipeViewModel", "No Deezer preview for: ${song.title}")
                }
            } catch (e: Exception) {
                Log.w("SwipeViewModel", "Error fetching Deezer preview for ${song.title}: ${e.message}")
            }
        }

        if (updated) {
            songs = enrichedSongs
            Log.d("SwipeViewModel", "Songs enriched with Deezer previews")
        }
    }
}