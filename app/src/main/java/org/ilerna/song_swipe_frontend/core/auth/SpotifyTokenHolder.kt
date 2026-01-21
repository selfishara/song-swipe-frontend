package org.ilerna.song_swipe_frontend.core.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ISpotifyTokenDataStore

/**
 * Thread-safe holder for the Spotify provider token with DataStore persistence
 * 
 * This is necessary because Supabase's importAuthToken() does not persist
 * the provider_token (Spotify token) in the session. We need to extract it
 * from the OAuth callback URL and store it separately.
 * 
 * This implementation uses DataStore for persistent storage while maintaining
 * an in-memory cache for synchronous access. The cache is synced with DataStore
 * on initialization and updates.
 * 
 * Thread Safety:
 * - Uses Mutex to ensure atomic updates of both in-memory cache and DataStore
 * - Initialization is idempotent and can be safely called multiple times
 * 
 * Usage:
 * 1. Call initialize() with a SpotifyTokenDataStore instance on app startup
 * 2. Call loadFromDataStore() to restore persisted tokens (returns success status)
 * 3. Use setTokens() to store tokens (persists to DataStore)
 * 4. Use getAccessToken()/getRefreshToken() for synchronous cached access
 * 5. Use accessTokenFlow/refreshTokenFlow for reactive updates
 * 
 * @throws IllegalStateException if setTokens() or clear() is called before initialize()
 * @see <a href="https://developer.android.com/topic/security/data">Android Security Best Practices</a>
 */
object SpotifyTokenHolder {
    
    private var tokenDataStore: ISpotifyTokenDataStore? = null
    
    // Mutex for thread-safe atomic operations on cache and DataStore
    private val mutex = Mutex()
    
    // Flag to track initialization state
    @Volatile
    private var _isInitialized = false
    
    // In-memory cache for synchronous access
    private val _accessTokenFlow = MutableStateFlow<String?>(null)
    private val _refreshTokenFlow = MutableStateFlow<String?>(null)
    
    /**
     * Flow that emits the current Spotify access token
     */
    val accessTokenFlow: Flow<String?> = _accessTokenFlow.asStateFlow()
    
    /**
     * Flow that emits the current Spotify refresh token
     */
    val refreshTokenFlow: Flow<String?> = _refreshTokenFlow.asStateFlow()
    
    /**
     * Checks if the token holder has been initialized
     */
    val isInitialized: Boolean
        get() = _isInitialized
    
    /**
     * Initializes the token holder with a DataStore instance.
     * This method is idempotent - calling it multiple times with the same
     * DataStore instance is safe and will not cause issues.
     * 
     * Should be called once during app startup before any token operations.
     * 
     * @param dataStore The SpotifyTokenDataStore instance for persistence
     */
    @Synchronized
    fun initialize(dataStore: ISpotifyTokenDataStore) {
        if (_isInitialized && tokenDataStore === dataStore) {
            // Already initialized with the same instance, no-op
            return
        }
        tokenDataStore = dataStore
        _isInitialized = true
    }
    
    /**
     * Loads tokens from DataStore into memory cache.
     * Should be called after initialize() to restore persisted tokens.
     * 
     * @return true if tokens were successfully loaded, false if not initialized
     *         or an error occurred during loading
     */
    suspend fun loadFromDataStore(): Boolean {
        val store = tokenDataStore
        if (store == null) {
            return false
        }
        return try {
            mutex.withLock {
                _accessTokenFlow.value = store.getAccessTokenSync()
                _refreshTokenFlow.value = store.getRefreshTokenSync()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Stores the Spotify tokens extracted from OAuth callback.
     * Persists to DataStore and updates in-memory cache atomically.
     * 
     * @param accessToken The Spotify access token to store
     * @param refreshToken The Spotify refresh token to store
     * @throws IllegalStateException if called before initialize()
     */
    suspend fun setTokens(accessToken: String?, refreshToken: String?) {
        val store = tokenDataStore
            ?: throw IllegalStateException(
                "SpotifyTokenHolder.setTokens() called before initialize(). " +
                "Call SpotifyTokenHolder.initialize() during app startup."
            )
        
        mutex.withLock {
            _accessTokenFlow.value = accessToken
            _refreshTokenFlow.value = refreshToken
            store.setTokens(accessToken, refreshToken)
        }
    }
    
    /**
     * Gets the current Spotify access token from cache
     * @return The Spotify access token or null if not available
     */
    fun getAccessToken(): String? = _accessTokenFlow.value
    
    /**
     * Gets the current Spotify refresh token from cache
     * @return The Spotify refresh token or null if not available
     */
    fun getRefreshToken(): String? = _refreshTokenFlow.value
    
    /**
     * Clears all stored tokens (call on logout).
     * Clears both in-memory cache and persisted DataStore atomically.
     * 
     * @throws IllegalStateException if called before initialize()
     */
    suspend fun clear() {
        val store = tokenDataStore
            ?: throw IllegalStateException(
                "SpotifyTokenHolder.clear() called before initialize(). " +
                "Call SpotifyTokenHolder.initialize() during app startup."
            )
        
        mutex.withLock {
            _accessTokenFlow.value = null
            _refreshTokenFlow.value = null
            store.clear()
        }
    }
    
    /**
     * Checks if a valid Spotify token is available in cache
     */
    fun hasToken(): Boolean = !_accessTokenFlow.value.isNullOrEmpty()
    
    /**
     * Clears in-memory cache only without persisting.
     * This is primarily for testing purposes.
     * For production logout, use clear() instead.
     */
    internal fun clearCacheOnly() {
        _accessTokenFlow.value = null
        _refreshTokenFlow.value = null
    }
    
    /**
     * Resets the holder to uninitialized state.
     * This is primarily for testing purposes.
     */
    internal fun reset() {
        _accessTokenFlow.value = null
        _refreshTokenFlow.value = null
        tokenDataStore = null
        _isInitialized = false
    }
}
