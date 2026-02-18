package org.ilerna.song_swipe_frontend.presentation.screen.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.ilerna.song_swipe_frontend.domain.usecase.playlist.GetOrCreateDefaultPlaylistUseCase
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
 * @param getOrCreateDefaultPlaylistUseCase Use case for ensuring a default playlist exists
 * @param supabaseUserId The Supabase auth user ID
 * @param spotifyUserId The Spotify user ID
 */
class SwipeViewModelFactory(
    private val getPlaylistTracksUseCase: GetPlaylistTracksUseCase,
    private val getTrackPreviewUseCase: GetTrackPreviewUseCase,
    private val getOrCreateDefaultPlaylistUseCase: GetOrCreateDefaultPlaylistUseCase,
    private val supabaseUserId: String,
    private val spotifyUserId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwipeViewModel::class.java)) {
            return SwipeViewModel(
                getPlaylistTracksUseCase,
                getTrackPreviewUseCase,
                getOrCreateDefaultPlaylistUseCase,
                supabaseUserId,
                spotifyUserId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
