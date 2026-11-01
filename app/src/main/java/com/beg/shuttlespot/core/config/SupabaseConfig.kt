package com.beg.shuttlespot.core.config

// Overview: Defines supabase config for the core config area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.BuildConfig

// Defines the supabase config type.
object SupabaseConfig {
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    // Handles the require configured responsibility.
    fun requireConfigured() {
        check(isConfigured) { "Supabase is not configured. Add values to local.properties; never commit them." }
    }
}
