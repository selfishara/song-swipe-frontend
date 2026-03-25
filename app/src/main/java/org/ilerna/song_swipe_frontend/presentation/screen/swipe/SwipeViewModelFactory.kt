package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.SwipeSessionDataStore
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.AddItemToDefaultPlaylistUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase

/**
 * Factory class for creating instances of [SwipeViewModel] with the required dependencies.
 */
class SwipeViewModelFactory(
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    private val getTrackPreviewUseCase: GetTrackPreviewUseCase,
    private val getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase,
    private val addItemToDefaultPlaylistUseCase: AddItemToDefaultPlaylistUseCase,
    private val swipeSessionDataStore: SwipeSessionDataStore,
    private val supabaseUserId: String,
    private val spotifyUserId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwipeViewModel::class.java)) {
            return SwipeViewModel(
                getPlaylistTracksUseCase = getPlaylistTracksUseCase,
                getTrackPreviewUseCase = getTrackPreviewUseCase,
                getOrCreateDefaultPlaylistUseCase = getOrCreateDefaultPlaylistUseCase,
                addItemToDefaultPlaylistUseCase = addItemToDefaultPlaylistUseCase,
                swipeSessionDataStore = swipeSessionDataStore,
                supabaseUserId = supabaseUserId,
                spotifyUserId = spotifyUserId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
