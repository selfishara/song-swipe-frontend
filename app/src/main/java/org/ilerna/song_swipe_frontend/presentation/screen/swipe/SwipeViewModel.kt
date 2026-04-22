package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.usecase.GetSkippedTrackIdsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.RecordSkipUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.swipe.ProcessSwipeLikeUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel

enum class SwipeDirection { LEFT, RIGHT }

/**
 * ViewModel handling swipe interactions logic.
 *
 * - RIGHT swipe: saves song and adds to the active playlist (selected by the user)
 * - LEFT swipe: discards song and persists the skip
 * - Without an active playlist, RIGHT swipes are blocked and a picker is shown instead
 *
 * Session state (genre) is persisted in DataStore so the user can resume swiping
 * after navigating away or closing the app. On resume, tracks are re-fetched
 * and re-shuffled for a fresh experience.
 */
class SwipeViewModel(
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    private val getTrackPreviewUseCase: GetTrackPreviewUseCase,
    private val processSwipeLikeUseCase: ProcessSwipeLikeUseCase,
    private val recordSkipUseCase: RecordSkipUseCase,
    private val getSkippedTrackIdsUseCase: GetSkippedTrackIdsUseCase,
    private val getUserPlaylistsUseCase: GetUserPlaylistsUseCase,
    private val getActivePlaylistUseCase: GetActivePlaylistUseCase,
    private val setActivePlaylistUseCase: SetActivePlaylistUseCase,
    private val swipeSessionDataStore: SwipeSessionDataStore,
    private val genrePlaylistProvider: GenrePlaylistProvider
) : ViewModel() {

    var songs by mutableStateOf<List<SongUiModel>>(emptyList())
        private set
    var currentIndex by mutableIntStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var hasSession by mutableStateOf(false)
        private set

    var activeGenre by mutableStateOf<String?>(null)
        private set

    var userPlaylists by mutableStateOf<List<Playlist>>(emptyList())
        private set

    var showPlaylistPicker by mutableStateOf(false)
        private set

    val likedSongs = mutableStateListOf<SongUiModel>()

    val activePlaylistId: StateFlow<String?> = getActivePlaylistUseCase.id()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val activePlaylistName: StateFlow<String?> = getActivePlaylistUseCase.name()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        restoreSession()
    }

    fun startSession(genre: String) {
        val playlistIds = genrePlaylistProvider.getPlaylistIdsForGenre(genre)
        if (playlistIds.isEmpty()) {
            Log.e("SwipeViewModel", "No playlists configured for genre: $genre")
            return
        }

        activeGenre = genre
        hasSession = true
        currentIndex = 0
        loadSongs(playlistIds)
        viewModelScope.launch { swipeSessionDataStore.saveGenre(genre) }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            val savedGenre = swipeSessionDataStore.getGenreSync()
            if (savedGenre != null) {
                val playlistIds = genrePlaylistProvider.getPlaylistIdsForGenre(savedGenre)
                if (playlistIds.isNotEmpty()) {
                    activeGenre = savedGenre
                    hasSession = true
                    loadSongs(playlistIds)
                }
            }
        }
    }

    fun openPlaylistPicker() {
        showPlaylistPicker = true
        loadUserPlaylists()
    }

    fun dismissPlaylistPicker() {
        showPlaylistPicker = false
    }

    private fun loadUserPlaylists() {
        viewModelScope.launch {
            when (val result = getUserPlaylistsUseCase()) {
                is NetworkResult.Success -> userPlaylists = result.data
                is NetworkResult.Error -> Log.e("SwipeViewModel", "Error loading playlists: ${result.message}")
                is NetworkResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun changeActivePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            setActivePlaylistUseCase(playlistId = playlist.id, playlistName = playlist.name)
            showPlaylistPicker = false
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
                Log.d("Swipe", "Discarded: ${song.id}")

                viewModelScope.launch {
                    when (val result = recordSkipUseCase(song.id)) {
                        is NetworkResult.Success -> {
                            Log.d("SwipeViewModel", "Skip guardado correctamente: ${song.id}")
                        }
                        is NetworkResult.Error -> {
                            Log.e("SwipeViewModel", "Error guardando skip: ${result.message}")
                        }
                        is NetworkResult.Loading -> { /* no-op */ }
                    }
                }

                next()
            }

            SwipeDirection.RIGHT -> {
                val playlistId = activePlaylistId.value
                if (playlistId.isNullOrBlank()) {
                    Log.w("SwipeViewModel", "Right swipe blocked — no active playlist")
                    openPlaylistPicker()
                    return
                }

                save(song)
                viewModelScope.launch {
                    try {
                        when (val result = processSwipeLikeUseCase.handle(
                            playlistId = playlistId,
                            trackId = song.id
                        )) {
                            is NetworkResult.Success -> Log.d("SwipeViewModel", "Song added to active playlist")
                            is NetworkResult.Error -> Log.e("SwipeViewModel", "Error adding song: ${result.message}")
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

        if (currentIndex >= songs.size) {
            viewModelScope.launch { swipeSessionDataStore.clearSession() }
            hasSession = false
            activeGenre = null
        }
    }

    private fun loadSongs(playlistIds: List<String>) {
        isLoading = true
        viewModelScope.launch {
            Log.d("SwipeViewModel", "Loading songs from ${playlistIds.size} playlist(s)...")

            val skippedIds: Set<String> = when (val skippedResult = getSkippedTrackIdsUseCase()) {
                is NetworkResult.Success -> skippedResult.data.toSet()
                is NetworkResult.Error -> {
                    Log.e("SwipeViewModel", "Error loading skips: ${skippedResult.message}")
                    emptySet()
                }
                is NetworkResult.Loading -> emptySet()
            }

            when (val result = getPlaylistTracksUseCase(playlistIds)) {
                is NetworkResult.Success -> {
                    val initialSongs = result.data
                        .filter { track -> track.id !in skippedIds }
                        .map { track ->
                            SongUiModel(
                                id = track.id,
                                title = track.name,
                                artist = track.artists.joinToString(", ") { it.name },
                                imageUrl = track.imageUrl,
                                previewUrl = track.previewUrl
                            )
                        }

                    songs = initialSongs
                    currentIndex = 0

                    if (songs.isEmpty()) {
                        hasSession = false
                        activeGenre = null
                        swipeSessionDataStore.clearSession()
                    }

                    isLoading = false
                    Log.d("SwipeViewModel", "Loaded ${songs.size} songs after filtering skips")

                    enrichWithDeezerPreviews(initialSongs)
                }

                is NetworkResult.Error -> {
                    isLoading = false
                    Log.e("SwipeViewModel", "Error loading songs: ${result.message}")
                }

                is NetworkResult.Loading -> Log.d("SwipeViewModel", "Loading...")
            }
        }
    }

    private suspend fun enrichWithDeezerPreviews(songList: List<SongUiModel>) {
        for (song in songList) {
            if (song.previewUrl != null) continue

            try {
                val previewResult = getTrackPreviewUseCase(
                    trackName = song.title,
                    artistName = song.artist.split(",").first().trim()
                )

                if (previewResult is NetworkResult.Success && previewResult.data != null) {
                    songs = songs.map { existingSong ->
                        if (existingSong.id == song.id) {
                            existingSong.copy(previewUrl = previewResult.data)
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