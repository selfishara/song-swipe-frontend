package org.ilerna.song_swipe_frontend.integration

import kotlinx.coroutines.test.runTest
import org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyCreatePlaylistRequestDto
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests that hit the real Spotify Web API.
 *
 * These tests validate that our Retrofit interface definitions,
 * DTO mappings, and request/response format match the live Spotify API.
 *
 * Requirements:
 * - Valid SPOTIFY_CLIENT_ID_TEST, SPOTIFY_CLIENT_SECRET_TEST, and
 *   SPOTIFY_REFRESH_TOKEN_TEST in local.properties
 * - Network access
 *
 * Tests are automatically skipped when credentials are missing.
 *
 * Run only integration tests:
 *   ./gradlew test --tests "*Integration*"
 */
class SpotifyApiIntegrationTest : BaseApiIntegrationTest() {

    // -- User Profile --------------------------------------------------------

    @Test
    fun `getUserProfile returns valid user with display name`() = runTest {
        val response = spotifyApi.getCurrentUserProfile()

        assertTrue(response.isSuccessful, "Expected 200, got ${response.code()}")
        val user = response.body()
        assertNotNull(user, "Response body should not be null")
        assertNotNull(user.id, "User ID should not be null")
        assertNotNull(user.displayName, "Display name should not be null")
    }

    // -- Playlist Tracks -----------------------------------------------------

    @Test
    fun `getPlaylistTracks returns tracks for a known public playlist`() = runTest {
        val knownPlaylistId = "7w0Fy9FiPOKFTYkZDPiY6R"

        val response = spotifyApi.getPlaylistTracksPaged(
            playlistId = knownPlaylistId,
            limit = 5
        )

        assertTrue(response.isSuccessful, "Expected 200, got ${response.code()}")
        val body = response.body()
        assertNotNull(body, "Response body should not be null")
        assertTrue(body.items.isNotEmpty(), "Playlist should contain at least one track")

        // Verify structure of the first track item
        val firstItem = body.items.first()
        assertNotNull(firstItem.track, "Track item should contain a track object")
        assertNotNull(firstItem.track.id, "Track should have an ID")
        assertNotNull(firstItem.track.name, "Track should have a name")
    }

    // -- Browse Categories ---------------------------------------------------

    @Test
    fun `getCategories returns at least one category`() = runTest {
        val response = spotifyApi.getCategories(limit = 5)

        assertTrue(response.isSuccessful, "Expected 200, got ${response.code()}")
        val body = response.body()
        assertNotNull(body, "Response body should not be null")
        assertTrue(body.categories.items.isNotEmpty(), "Should have at least one category")

        val first = body.categories.items.first()
        assertNotNull(first.id, "Category should have an ID")
        assertNotNull(first.name, "Category should have a name")
    }

    // -- Create Playlist + Add Tracks + Verify -------------------------------

    @Test
    fun `create playlist, add track, and verify track exists`() = runTest {
        // 1. Get current user ID
        val profileResponse = spotifyApi.getCurrentUserProfile()
        assertTrue(profileResponse.isSuccessful)
        val userId = profileResponse.body()!!.id

        // 2. Create a test playlist
        val createRequest = SpotifyCreatePlaylistRequestDto(
            name = "SongSwipe Integration Test - ${System.currentTimeMillis()}",
            description = "Auto-created by integration test — safe to delete"
            // public = false
        )
        val createResponse = spotifyApi.createPlaylist(userId, createRequest)
        assertNotNull(createResponse, "Create playlist response should not be null")
        val playlistId = createResponse.id
        assertNotNull(playlistId, "Created playlist ID should not be null")

        // 3. Add a track (Bohemian Rhapsody)
        val trackUri = "spotify:track:7tFiyTwD0nx5a1eklYtX2J"
        val addRequest = org.ilerna.song_swipe_frontend.data.datasource.remote.dto.SpotifyAddItemsRequestDto(
            uris = listOf(trackUri)
        )
        val addResponse = spotifyApi.addItemsToPlaylist(playlistId, addRequest)
        assertTrue(addResponse.isSuccessful, "Add items should succeed, got ${addResponse.code()}")
        assertNotNull(addResponse.body()?.snapshotId, "Should return a snapshot ID")

        // 4. Verify track is in the playlist
        val tracksResponse = spotifyApi.getPlaylistTracksPaged(playlistId, limit = 10)
        assertTrue(tracksResponse.isSuccessful)
        val tracks = tracksResponse.body()?.items ?: emptyList()
        assertTrue(
            tracks.any { it.track?.uri == trackUri },
            "Playlist should contain the track we just added"
        )
    }

    // -- Error handling -------------------------------------------------------

    @Test
    fun `requesting non-existent playlist returns error`() = runTest {
        // Use a valid Base62 format ID that doesn't exist
        val response = spotifyApi.getPlaylistTracksPaged(
            playlistId = "0000000000000000000000",
            limit = 1
        )

        assertTrue(
            !response.isSuccessful,
            "Expected error for non-existent playlist, got ${response.code()}"
        )
    }
}
