package org.ilerna.song_swipe_frontend.core.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.ilerna.song_swipe_frontend.data.datasource.local.preferences.ISpotifyTokenDataStore

/**
 * Fake implementation of ISpotifyTokenDataStore for testing.
 * Uses in-memory storage instead of DataStore.
 */
class FakeSpotifyTokenDataStore : ISpotifyTokenDataStore {
    
    private val _accessToken = MutableStateFlow<String?>(null)
    private val _refreshToken = MutableStateFlow<String?>(null)
    
    override val accessToken: Flow<String?> = _accessToken
    override val refreshToken: Flow<String?> = _refreshToken
    
    override suspend fun setTokens(accessToken: String?, refreshToken: String?) {
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
    }
    
    override suspend fun getAccessTokenSync(): String? = _accessToken.value
    
    override suspend fun getRefreshTokenSync(): String? = _refreshToken.value
    
    override suspend fun hasToken(): Boolean = !_accessToken.value.isNullOrEmpty()
    
    override suspend fun clear() {
        _accessToken.value = null
        _refreshToken.value = null
    }
}
