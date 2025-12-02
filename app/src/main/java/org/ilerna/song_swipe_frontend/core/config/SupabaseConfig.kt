package org.ilerna.song_swipe_frontend.core.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Supabase configuration and client initialization
 * 
 * Project: song-swipe
 * Project ID: keogusadivocspsdysez
 */
object SupabaseConfig {
    
    /**
     * Supabase project URL
     */
    const val SUPABASE_URL = "https://keogusadivocspsdysez.supabase.co"
    
    /**
     * Supabase anonymous/public key (safe for client-side use)
     */
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtlb2d1c2FkaXZvY3Nwc2R5c2V6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM3NTMwMDUsImV4cCI6MjA3OTMyOTAwNX0.RHVqqV4xcQgTAqVZHaEbqa5ugkT4ViRqmL7eJDxERTE"
    
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
