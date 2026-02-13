package org.ilerna.song_swipe_frontend.presentation.components.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Playback state exposed to the UI.
 */
enum class PlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    ERROR
}

/**
 * Lightweight audio player for 30-second Deezer preview clips.
 *
 * Uses Android's built-in [MediaPlayer] — no external dependencies needed.
 * Designed to be used as a singleton shared across the swipe session,
 * automatically stopping the previous track when a new one is played.
 *
 * Lifecycle: call [release] when the composable/screen is disposed.
 */
class PreviewAudioPlayer(
    private val mediaPlayerFactory: () -> MediaPlayer = { MediaPlayer() }
) {

    companion object {
        private const val TAG = "PreviewAudioPlayer"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    /** Playback progress from 0.0 to 1.0 */
    val progress: StateFlow<Float> = _progress.asStateFlow()

    /**
     * Plays a preview URL. If the same URL is already playing, toggles pause/resume.
     * If a different URL is provided, stops the current track and starts the new one.
     *
     * @param url The MP3 preview URL to play
     */
    fun playOrToggle(url: String) {
        if (url == currentUrl && mediaPlayer != null) {
            togglePauseResume()
            return
        }

        // New track — stop previous and start fresh
        stop()
        currentUrl = url
        _playbackState.value = PlaybackState.LOADING

        try {
            mediaPlayer = mediaPlayerFactory().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)

                setOnPreparedListener {
                    start()
                    _playbackState.value = PlaybackState.PLAYING
                    Log.d(TAG, "Playing preview: $url")
                }

                setOnCompletionListener {
                    _playbackState.value = PlaybackState.IDLE
                    _progress.value = 0f
                    Log.d(TAG, "Preview playback completed")
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    _playbackState.value = PlaybackState.ERROR
                    _progress.value = 0f
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up MediaPlayer: ${e.message}")
            _playbackState.value = PlaybackState.ERROR
        }
    }

    /**
     * Toggles between pause and resume for the current track.
     */
    private fun togglePauseResume() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = PlaybackState.PAUSED
            } else {
                player.start()
                _playbackState.value = PlaybackState.PLAYING
            }
        }
    }

    /**
     * Stops the current playback and resets state.
     */
    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping MediaPlayer: ${e.message}")
        }
        mediaPlayer = null
        currentUrl = null
        _playbackState.value = PlaybackState.IDLE
        _progress.value = 0f
    }

    /**
     * Updates the progress value. Should be called periodically (e.g., every 250ms)
     * from a coroutine while playback is active.
     */
    fun updateProgress() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying && player.duration > 0) {
                    _progress.value = player.currentPosition.toFloat() / player.duration.toFloat()
                }
            } catch (e: Exception) {
                // Player may have been released between the check and the call
            }
        }
    }

    /**
     * Releases all resources. Must be called when the screen is disposed.
     */
    fun release() {
        stop()
        Log.d(TAG, "PreviewAudioPlayer released")
    }
}
