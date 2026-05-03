package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.ilerna.song_swipe_frontend.core.analytics.AnalyticsManager
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.data.provider.GenrePlaylistProvider
import org.ilerna.song_swipe_frontend.domain.usecase.GetSkippedTrackIdsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.RecordSkipUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetUserPlaylistsUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.SetActivePlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.swipe.ProcessSwipeLikeUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.StreamPlaylistTracksUseCase

class SwipeViewModelFactory(
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
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwipeViewModel::class.java)) {
            return SwipeViewModel(
                streamPlaylistTracksUseCase = streamPlaylistTracksUseCase,
                getTrackPreviewUseCase = getTrackPreviewUseCase,
                processSwipeLikeUseCase = processSwipeLikeUseCase,
                recordSkipUseCase = recordSkipUseCase,
                getSkippedTrackIdsUseCase = getSkippedTrackIdsUseCase,
                getUserPlaylistsUseCase = getUserPlaylistsUseCase,
                getActivePlaylistUseCase = getActivePlaylistUseCase,
                setActivePlaylistUseCase = setActivePlaylistUseCase,
                swipeSessionDataStore = swipeSessionDataStore,
                genrePlaylistProvider = genrePlaylistProvider,
                analyticsManager = analyticsManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}