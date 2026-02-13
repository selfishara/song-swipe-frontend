package org.ilerna.song_swipe_frontend.presentation.components.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [PreviewAudioPlayer].
 *
 * Injects a mock [MediaPlayer] via factory, allowing full state-transition
 * testing without an Android environment.
 */
class PreviewAudioPlayerTest {

    private lateinit var player: PreviewAudioPlayer
    private lateinit var mockMediaPlayer: MediaPlayer

    // Captured listeners for simulating MediaPlayer callbacks
    private var onPreparedListener: MediaPlayer.OnPreparedListener? = null
    private var onCompletionListener: MediaPlayer.OnCompletionListener? = null
    private var onErrorListener: MediaPlayer.OnErrorListener? = null

    @Before
    fun setUp() {
        // Mock AudioAttributes.Builder to avoid null pointer exceptions
        mockkConstructor(AudioAttributes.Builder::class)
        every { anyConstructed<AudioAttributes.Builder>().setContentType(any()) } answers { self as AudioAttributes.Builder }
        every { anyConstructed<AudioAttributes.Builder>().setUsage(any()) } answers { self as AudioAttributes.Builder }
        every { anyConstructed<AudioAttributes.Builder>().build() } returns mockk(relaxed = true)

        mockMediaPlayer = mockk(relaxed = true)

        // Capture listeners when they are set
        every { mockMediaPlayer.setOnPreparedListener(any()) } answers {
            onPreparedListener = firstArg()
        }
        every { mockMediaPlayer.setOnCompletionListener(any()) } answers {
            onCompletionListener = firstArg()
        }
        every { mockMediaPlayer.setOnErrorListener(any()) } answers {
            onErrorListener = firstArg()
        }

        // Default stubs
        every { mockMediaPlayer.isPlaying } returns false
        every { mockMediaPlayer.currentPosition } returns 0
        every { mockMediaPlayer.duration } returns 30_000

        player = PreviewAudioPlayer(mediaPlayerFactory = { mockMediaPlayer })
    }

    @After
    fun tearDown() {
        unmockkConstructor(AudioAttributes.Builder::class)
    }

    // Initial state tests

    @Test
    fun `initial playback state is IDLE`() = runTest {
        assertEquals(PlaybackState.IDLE, player.playbackState.first())
    }

    @Test
    fun `initial progress is zero`() = runTest {
        assertEquals(0f, player.progress.first())
    }

    // playOrToggle – new track tests

    @Test
    fun `playOrToggle with new URL sets state to LOADING`() {
        player.playOrToggle("https://example.com/preview.mp3")

        assertEquals(PlaybackState.LOADING, player.playbackState.value)
    }

    @Test
    fun `playOrToggle transitions to PLAYING when MediaPlayer is prepared`() {
        player.playOrToggle("https://example.com/preview.mp3")

        // Simulate MediaPlayer calling onPrepared
        onPreparedListener!!.onPrepared(mockMediaPlayer)

        assertEquals(PlaybackState.PLAYING, player.playbackState.value)
    }

    @Test
    fun `onCompletion resets state to IDLE and progress to zero`() {
        player.playOrToggle("https://example.com/preview.mp3")
        onPreparedListener!!.onPrepared(mockMediaPlayer)

        // Simulate track ending
        onCompletionListener!!.onCompletion(mockMediaPlayer)

        assertEquals(PlaybackState.IDLE, player.playbackState.value)
        assertEquals(0f, player.progress.value)
    }

    @Test
    fun `onError sets state to ERROR and resets progress`() {
        player.playOrToggle("https://example.com/preview.mp3")

        // Simulate MediaPlayer error
        onErrorListener!!.onError(mockMediaPlayer, 1, 0)

        assertEquals(PlaybackState.ERROR, player.playbackState.value)
        assertEquals(0f, player.progress.value)
    }

    // playOrToggle – same track toggle tests

    @Test
    fun `playOrToggle same URL while playing pauses playback`() {
        val url = "https://example.com/preview.mp3"

        player.playOrToggle(url)
        onPreparedListener!!.onPrepared(mockMediaPlayer)

        // Now playing – toggle should pause
        every { mockMediaPlayer.isPlaying } returns true
        player.playOrToggle(url)

        assertEquals(PlaybackState.PAUSED, player.playbackState.value)
        verify { mockMediaPlayer.pause() }
    }

    @Test
    fun `playOrToggle same URL while paused resumes playback`() {
        val url = "https://example.com/preview.mp3"

        player.playOrToggle(url)
        onPreparedListener!!.onPrepared(mockMediaPlayer)

        // Pause first
        every { mockMediaPlayer.isPlaying } returns true
        player.playOrToggle(url)

        // Resume
        every { mockMediaPlayer.isPlaying } returns false
        player.playOrToggle(url)

        assertEquals(PlaybackState.PLAYING, player.playbackState.value)
    }

    // playOrToggle – switching tracks tests

    @Test
    fun `playOrToggle with different URL stops previous and starts loading`() {
        val url1 = "https://example.com/track1.mp3"
        val url2 = "https://example.com/track2.mp3"

        player.playOrToggle(url1)
        onPreparedListener!!.onPrepared(mockMediaPlayer)

        // Switch to second track
        every { mockMediaPlayer.isPlaying } returns true
        player.playOrToggle(url2)

        assertEquals(PlaybackState.LOADING, player.playbackState.value)
    }

    // stop tests

    @Test
    fun `stop resets state to IDLE and progress to zero`() {
        player.playOrToggle("https://example.com/preview.mp3")
        onPreparedListener!!.onPrepared(mockMediaPlayer)

        player.stop()

        assertEquals(PlaybackState.IDLE, player.playbackState.value)
        assertEquals(0f, player.progress.value)
    }

    // updateProgress tests

    @Test
    fun `updateProgress calculates correct fraction`() {
        player.playOrToggle("https://example.com/preview.mp3")
        onPreparedListener!!.onPrepared(mockMediaPlayer)

        every { mockMediaPlayer.isPlaying } returns true
        every { mockMediaPlayer.currentPosition } returns 15_000
        every { mockMediaPlayer.duration } returns 30_000

        player.updateProgress()

        assertEquals(0.5f, player.progress.value)
    }

    @Test
    fun `updateProgress does nothing when not playing`() {
        player.playOrToggle("https://example.com/preview.mp3")
        onPreparedListener!!.onPrepared(mockMediaPlayer)

        every { mockMediaPlayer.isPlaying } returns false

        player.updateProgress()

        assertEquals(0f, player.progress.value)
    }

    // release tests

    @Test
    fun `release resets all state`() {
        player.playOrToggle("https://example.com/preview.mp3")
        onPreparedListener!!.onPrepared(mockMediaPlayer)

        player.release()

        assertEquals(PlaybackState.IDLE, player.playbackState.value)
        assertEquals(0f, player.progress.value)
    }
}
