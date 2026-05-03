package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import android.util.Log
import androidx.compose.runtime.*
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
import org.ilerna.song_swipe_frontend.domain.usecase.GetSkippedTrackIdsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.RecordSkipUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.swipe.ProcessSwipeLikeUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.StreamPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.presentation.screen.swipe.model.SongUiModel

enum class SwipeDirection { LEFT, RIGHT }

class SwipeViewModel(
    private val streamPlaylistTracksUseCase: StreamPlaylistTracksUseCase,
    private val getTrackPreviewUseCase: GetTrackPreviewUseCase,
    private val processSwipeLikeUseCase: ProcessSwipeLikeUseCase,
    private val recordSkipUseCase: RecordSkipUseCase,
    private val getSkippedTrackIdsUseCase: GetSkippedTrackIdsUseCase,
    private val getUserPlaylistsUseCase: GetUserPlaylistsUseCase,
    private val getActivePlaylistUseCase: GetActivePlaylistUseCase,
    private val setActivePlaylistUseCase: SetActivePlaylistUseCase,
    private val swipeSessionDataStore: SwipeSessionDataStore,
    private val genrePlaylistProvider: GenrePlaylistProvider,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val previewMergeMutex = Mutex()

    private var skippedTrackIds by mutableStateOf<Set<String>>(emptySet())

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

    val activePlaylistId: StateFlow<String?> =
        getActivePlaylistUseCase.id().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val activePlaylistName: StateFlow<String?> =
        getActivePlaylistUseCase.name().stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
                val ids = genrePlaylistProvider.getPlaylistIdsForGenre(savedGenre)
                if (ids.isNotEmpty()) {
                    activeGenre = savedGenre
                    hasSession = true
                    loadSongs(ids)
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
                is NetworkResult.Error -> Log.e("SwipeVM", result.message)
                else -> {}
            }
        }
    }

    fun changeActivePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            setActivePlaylistUseCase(playlist.id, playlist.name)
            showPlaylistPicker = false
        }
    }

    fun nextSongs(count: Int): List<SongUiModel> {
        val from = currentIndex + 1
        val to = minOf(from + count, songs.size)
        return if (from < songs.size) songs.subList(from, to) else emptyList()
    }

    fun currentSongOrNull(): SongUiModel? = songs.getOrNull(currentIndex)

    fun onSwipe(direction: SwipeDirection) {
        val song = currentSongOrNull() ?: return

        when (direction) {
            SwipeDirection.LEFT -> {
                viewModelScope.launch {
                    when (val result = recordSkipUseCase(song.id)) {
                        is NetworkResult.Success -> {
                            skippedTrackIds = skippedTrackIds + song.id
                            Log.d("SwipeVM", "Skip saved: ${song.id}")
                        }

                        is NetworkResult.Error -> {
                            Log.e("SwipeVM", "Error saving skip: ${result.message}")
                        }

                        else -> {}
                    }
                }

                next()
            }

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
        skippedTrackIds = emptySet()

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var analyticsLogged = false

            skippedTrackIds = when (val result = getSkippedTrackIdsUseCase()) {
                is NetworkResult.Success -> result.data.toSet()
                is NetworkResult.Error -> {
                    Log.e("SwipeVM", "Error loading skips: ${result.message}")
                    emptySet()
                }
                else -> emptySet()
            }

            try {
                streamPlaylistTracksUseCase(playlistIds).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            handleTrackBatch(result.data)

                            if (!analyticsLogged && songs.isNotEmpty()) {
                                analyticsLogged = true
                                val duration = System.currentTimeMillis() - startTime

                                analyticsManager.logInitialTracksLoadTime(
                                    durationMs = duration,
                                    trackCount = songs.size,
                                    playlistCount = playlistIds.size
                                )
                            }
                        }

                        is NetworkResult.Error ->
                            Log.e("SwipeVM", result.message)

                        else -> {}
                    }
                }
            } finally {
                if (isLoading) {
                    isLoading = false
                    if (songs.isEmpty()) {
                        hasSession = false
                        activeGenre = null
                        swipeSessionDataStore.clearSession()
                    }
                }
            }
        }
    }

    private fun handleTrackBatch(tracks: List<Track>) {
        val existing = songs.associateBy { it.id }

        val freshTracks = tracks.filter { track ->
            track.id !in skippedTrackIds
        }

        val merged = freshTracks.map {
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

        if (isLoading && songs.size >= 5) {
            isLoading = false
        }

        if (newSongs.isNotEmpty()) {
            enrichWithDeezerPreviews(newSongs)
        }
    }

    private fun enrichWithDeezerPreviews(songList: List<SongUiModel>) {
        val missing = songList.filter { it.previewUrl == null }
        if (missing.isEmpty()) return

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
                                        if (it.id == song.id)
                                            it.copy(previewUrl = result.data)
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