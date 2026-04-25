package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager
import org.ilerna.song_swipe_frontend.core.network.NetworkResult
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider
import org.ilerna.song_swipe_frontend.domain.model.Playlist
import org.ilerna.song_swipe_frontend.domain.model.Track
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.swipe.ProcessSwipeLikeUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.StreamPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel

enum class SwipeDirection { LEFT, RIGHT }

/**
 * ViewModel handling swipe interactions logic.
 *
 * - RIGHT swipe: saves song and adds to the active playlist
 * - LEFT swipe: discards song
 * - Without an active playlist, RIGHT swipes are blocked
 *
 * Tracks are streamed progressively for better UX.
 */
class SwipeViewModel(
    private val streamPlaylistTracksUseCase: StreamPlaylistTracksUseCase,
    private val getTrackPreviewUseCase: GetTrackPreviewUseCase,
    private val processSwipeLikeUseCase: ProcessSwipeLikeUseCase,
    private val getUserPlaylistsUseCase: GetUserPlaylistsUseCase,
    private val getActivePlaylistUseCase: GetActivePlaylistUseCase,
    private val setActivePlaylistUseCase: SetActivePlaylistUseCase,
    private val swipeSessionDataStore: SwipeSessionDataStore,
    private val genrePlaylistProvider: GenrePlaylistProvider,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val previewMergeMutex = Mutex()

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
        if (playlistIds.isEmpty()) return

        activeGenre = genre
        hasSession = true
        currentIndex = 0
        loadSongs(playlistIds)

        viewModelScope.launch {
            swipeSessionDataStore.saveGenre(genre)
        }
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

    private fun loadUserPlaylists() {
        viewModelScope.launch {
            when (val result = getUserPlaylistsUseCase()) {
                is NetworkResult.Success -> userPlaylists = result.data
                is NetworkResult.Error -> Log.e("SwipeViewModel", result.message)
                else -> {}
            }
        }
    }

    fun currentSongOrNull(): SongUiModel? = songs.getOrNull(currentIndex)

    fun onSwipe(direction: SwipeDirection) {
        val song = currentSongOrNull() ?: return

        when (direction) {
            SwipeDirection.LEFT -> next()

            SwipeDirection.RIGHT -> {
                val playlistId = activePlaylistId.value
                if (playlistId.isNullOrBlank()) {
                    openPlaylistPicker()
                    return
                }

                save(song)

                viewModelScope.launch {
                    processSwipeLikeUseCase.handle(playlistId, song.id)
                }

                next()
            }
        }
    }

    private fun save(song: SongUiModel) {
        if (likedSongs.none { it.id == song.id }) likedSongs.add(song)
    }

    private fun next() {
        currentIndex++

        if (currentIndex >= songs.size) {
            viewModelScope.launch { swipeSessionDataStore.clearSession() }
            hasSession = false
            activeGenre = null
        }
    }

    private fun loadSongs(playlistIds: List<String>) {
        isLoading = true
        songs = emptyList()
        currentIndex = 0

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var analyticsLogged = false

            streamPlaylistTracksUseCase(playlistIds).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        handleTrackBatch(result.data)

                        if (!analyticsLogged && songs.isNotEmpty()) {
                            val durationMs = System.currentTimeMillis() - startTime
                            analyticsLogged = true

                            analyticsManager.logInitialTracksLoadTime(
                                durationMs = durationMs,
                                trackCount = songs.size,
                                playlistCount = playlistIds.size
                            )
                        }
                    }

                    is NetworkResult.Error -> Log.e("SwipeViewModel", result.message)
                    else -> {}
                }
            }
        }
    }

    private fun handleTrackBatch(tracks: List<Track>) {
        val existing = songs.associateBy { it.id }

        val merged = tracks.map {
            existing[it.id] ?: SongUiModel(
                id = it.id,
                title = it.name,
                artist = it.artists.joinToString { a -> a.name },
                imageUrl = it.imageUrl,
                previewUrl = it.previewUrl
            )
        }

        val newSongs = merged.filter { it.id !in existing }
        songs = merged

        if (newSongs.isNotEmpty()) {
            enrichWithDeezerPreviews(newSongs)
        }
    }

    private fun enrichWithDeezerPreviews(songList: List<SongUiModel>) {
        val missing = songList.filter { it.previewUrl == null }

        viewModelScope.launch {
            val semaphore = Semaphore(5)

            coroutineScope {
                missing.forEach { song ->
                    launch {
                        semaphore.withPermit {
                            val result = getTrackPreviewUseCase(song.title, song.artist)
                            if (result is NetworkResult.Success && result.data != null) {
                                previewMergeMutex.withLock {
                                    songs = songs.map {
                                        if (it.id == song.id) it.copy(previewUrl = result.data)
                                        else it
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}