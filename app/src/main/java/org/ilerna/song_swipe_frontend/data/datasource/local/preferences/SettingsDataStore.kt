package org.ilerna.song_swipe_frontend.data.datasource.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Extension property to create DataStore instance
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Interface for settings data store operations
 * Allows for easier testing and dependency injection
 */
interface ISettingsDataStore {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(themeMode: ThemeMode)
}

/**
 * SettingsDataStore - Manages app settings using DataStore Preferences
 * Provides persistent storage for user preferences like theme mode
 */
open class SettingsDataStore(private val context: Context) : ISettingsDataStore {
    
    companion object {
        private const val TAG = "SettingsDataStore"
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
    
    /**
     * Flow that emits the current theme mode
     */
    override val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Invalid theme mode found in preferences: '$themeName'. Falling back to SYSTEM.", e)
            ThemeMode.SYSTEM
        }
    }
    
    /**
     * Save the selected theme mode to DataStore
     */
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }
}

/**
 * ThemeMode - Enum for theme selection
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;
    
    fun getDisplayName(): String = when (this) {
        LIGHT -> "Light"
        DARK -> "Dark"
        SYSTEM -> "System Default"
    }
}
