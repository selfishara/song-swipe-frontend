package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.swipe.ProcessSwipeLikeUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase

/**
 * Factory class for creating instances of [SwipeViewModel] with the required dependencies.
 */
class SwipeViewModelFactory(
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    private val getTrackPreviewUseCase: GetTrackPreviewUseCase,
    private val processSwipeLikeUseCase: ProcessSwipeLikeUseCase,
    private val getUserPlaylistsUseCase: GetUserPlaylistsUseCase,
    private val getActivePlaylistUseCase: GetActivePlaylistUseCase,
    private val setActivePlaylistUseCase: SetActivePlaylistUseCase,
    private val swipeSessionDataStore: SwipeSessionDataStore,
    private val genrePlaylistProvider: GenrePlaylistProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwipeViewModel::class.java)) {
            return SwipeViewModel(
                getPlaylistTracksUseCase = getPlaylistTracksUseCase,
                getTrackPreviewUseCase = getTrackPreviewUseCase,
                processSwipeLikeUseCase = processSwipeLikeUseCase,
                getUserPlaylistsUseCase = getUserPlaylistsUseCase,
                getActivePlaylistUseCase = getActivePlaylistUseCase,
                setActivePlaylistUseCase = setActivePlaylistUseCase,
                swipeSessionDataStore = swipeSessionDataStore,
                genrePlaylistProvider = genrePlaylistProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
