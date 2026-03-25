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
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.AddItemToDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel
enum class SwipeDirection { LEFT, RIGHT }

/**
 * ViewModel handling swipe interactions logic.
 *
 * - RIGHT swipe: saves song and adds to default playlist
 * - LEFT swipe: discards song
 *
 * Session state (playlist, genre, current index) is persisted in DataStore
 * so the user can resume swiping after navigating away or closing the app.
 */

class SwipeViewModel(
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    private val getTrackPreviewUseCase: GetTrackPreviewUseCase,
    private val getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase,
    private val addItemToDefaultPlaylistUseCase: AddItemToDefaultPlaylistUseCase,
    private val swipeSessionDataStore: SwipeSessionDataStore,
    private val supabaseUserId: String,
    private val spotifyUserId: String
): ViewModel() {

    var songs by mutableStateOf<List<SongUiModel>>(emptyList())
        private set
    var currentIndex by mutableIntStateOf(0)
        private set

    // true while we are loading a playlist or restoring a session
    var isLoading by mutableStateOf(false)
        private set

    // true when there is an active session (playlist loaded or being loaded)
    var hasSession by mutableStateOf(false)
        private set

    // The genre name for the current session, if any
    var activeGenre by mutableStateOf<String?>(null)
        private set

    // The playlist ID for the current session, if any
    private var activePlaylistId: String? = null

    val likedSongs = mutableStateListOf<SongUiModel>()

    init {
        restoreSession()
        initializeDefaultPlaylist()
    }

    /**
     * Called from the Vibe screen when the user picks a genre.
     * Starts a new session, discarding any previous one.
     */
    fun startSession(playlistId: String, genre: String) {
        activePlaylistId = playlistId
        activeGenre = genre
        hasSession = true
        loadSongs(playlistId)
        viewModelScope.launch {
            swipeSessionDataStore.saveSession(playlistId, genre, 0)
        }
    }

    /**
     * Restores a previous session from DataStore on first launch.
     */
    private fun restoreSession() {
        viewModelScope.launch {
            val savedPlaylistId = swipeSessionDataStore.getPlaylistIdSync()
            val savedGenre = swipeSessionDataStore.getGenreSync()
            val savedIndex = swipeSessionDataStore.getCurrentIndexSync()

            if (savedPlaylistId != null && savedGenre != null) {
                activePlaylistId = savedPlaylistId
                activeGenre = savedGenre
                hasSession = true
                loadSongs(savedPlaylistId, savedIndex)
            }
        }
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

    fun nextSongs(count: Int): List<SongUiModel> {
        val from = currentIndex + 1
        val to = minOf(from + count, songs.size)
        return if (from < songs.size) songs.subList(from, to) else emptyList()
    }

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
                viewModelScope.launch {
                    try {
                        when (val result = addItemToDefaultPlaylistUseCase(
                            supabaseUserId = supabaseUserId,
                            spotifyUserId = spotifyUserId,
                            trackId = song.id
                        )) {
                            is NetworkResult.Success -> {
                                Log.d("SwipeViewModel", "Song added to default playlist")
                            }
                            is NetworkResult.Error -> {
                                Log.e("SwipeViewModel", "Error adding song: ${result.message}")
                            }
                            is NetworkResult.Loading -> { /* no-op */ }
                        }
                    } catch (e: Exception) {
                        Log.e("SwipeViewModel", "Exception adding song", e)
                    }
                }
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

        // If we finished the playlist, clear the session
        if (currentIndex >= songs.size) {
            viewModelScope.launch {
                swipeSessionDataStore.clearSession()
            }
            hasSession = false
            activeGenre = null
            activePlaylistId = null
        } else {
            // Persist progress
            viewModelScope.launch {
                swipeSessionDataStore.saveCurrentIndex(currentIndex)
            }
        }
    }


    // --- Tracks logic --- //

    private fun loadSongs(playlistId: String, restoreIndex: Int = 0) {
        isLoading = true
        viewModelScope.launch {
            Log.d("SwipeViewModel", "Loading songs from playlist $playlistId...")
            when (val result = getPlaylistTracksUseCase(playlistId)) {

                is NetworkResult.Success -> {
                    val initialSongs = result.data.map { track ->
                        SongUiModel(
                            id = track.id,
                            title = track.name,
                            artist = track.artists.joinToString(", ") { it.name },
                            imageUrl = track.imageUrl,
                            previewUrl = track.previewUrl
                        )
                    }
                    songs = initialSongs

                    // Restore to saved index if valid, otherwise start from 0
                    currentIndex = if (restoreIndex in initialSongs.indices) restoreIndex else 0

                    // If restored index is past the end, session is finished
                    if (currentIndex >= songs.size && songs.isNotEmpty()) {
                        hasSession = false
                        activeGenre = null
                        activePlaylistId = null
                        swipeSessionDataStore.clearSession()
                    }

                    isLoading = false
                    Log.d("SwipeViewModel", "Loaded ${songs.size} songs, resuming at index $currentIndex")

                    enrichWithDeezerPreviews(initialSongs)
                }

                is NetworkResult.Error -> {
                    isLoading = false
                    Log.e("SwipeViewModel", "Error loading songs: ${result.message}")
                }

                is NetworkResult.Loading -> {
                    Log.d("SwipeViewModel", "Loading...")
                }
            }
        }
    }

    /**
     * Enriches songs that lack a preview URL by fetching from Deezer.
     * Updates the songs list progressively as previews are found.
     */
    private suspend fun enrichWithDeezerPreviews(songList: List<SongUiModel>) {


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
                    songs = songs.map { existingSong ->
                        if (existingSong.id == song.id) {
                            existingSong.copy(
                                previewUrl = previewResult.data
                            )
                        } else {
                            existingSong
                        }
                    }

                    Log.d("SwipeViewModel", "Preview updated for: ${song.title}")
                }

            } catch (e: Exception) {
                Log.w("SwipeViewModel", "Error fetching preview for ${song.title}")
            }

        }

    }
}