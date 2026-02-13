package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetPlaylistTracksUseCase
import org.ilerna.song_swipe_frontend.domain.usecase.tracks.GetTrackPreviewUseCase

/**
 * Factory for creating SwipeViewModel with its dependencies.
 *
 * Required because the project uses manual DI (no Hilt).
 * ViewModelProvider needs a no-arg constructor or a custom factory.
 *
 * @param getPlaylistTracksUseCase Use case for fetching playlist tracks
 * @param getTrackPreviewUseCase Use case for fetching track previews from Deezer
 */
class SwipeViewModelFactory(
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    private val getTrackPreviewUseCase: GetTrackPreviewUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwipeViewModel::class.java)) {
            return SwipeViewModel(getPlaylistTracksUseCase, getTrackPreviewUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
