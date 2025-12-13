package org.ilerna.song_swipe_frontend.core.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import org.ilerna.song_swipe_frontend.BuildConfig

/**
 * Supabase configuration and client initialization
 *
 * This configuration now uses BuildConfig to read credentials from local.properties
 * which allows for different environments (DEV and TEST) without exposing sensitive data in the code.
 *
 * To configure your environment:
 * 1. Copy local.properties.example to local.properties
 * 2. Fill in your Supabase credentials
 * 3. Set ACTIVE_ENVIRONMENT to "DEV" or "TEST" in local.properties
 */
object SupabaseConfig {

    /**
     * Current active environment
     * Set in local.properties: ACTIVE_ENVIRONMENT=DEV or ACTIVE_ENVIRONMENT=TEST
     */
    private val activeEnvironment = BuildConfig.ACTIVE_ENVIRONMENT

    /**
     * Supabase project URL based on active environment
     */
    val SUPABASE_URL: String = when (activeEnvironment) {
        "TEST" -> BuildConfig.SUPABASE_URL_TEST
        else -> BuildConfig.SUPABASE_URL_DEV // Default to DEV
    }

    /**
     * Supabase anonymous/public key based on active environment
     * This key is safe for client-side use
     */
    val SUPABASE_ANON_KEY: String = when (activeEnvironment) {
        "TEST" -> BuildConfig.SUPABASE_ANON_KEY_TEST
        else -> BuildConfig.SUPABASE_ANON_KEY_DEV // Default to DEV
    }

    /**
     * Supabase client instance
     * Initialized lazily on first access
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            // Install Auth plugin for authentication
            install(Auth) {
                // Deep link configuration for OAuth callback
                scheme = "songswipe"
                host = "callback"
            }

            // Install Postgrest plugin for database operations
            install(Postgrest)
        }
    }
}
