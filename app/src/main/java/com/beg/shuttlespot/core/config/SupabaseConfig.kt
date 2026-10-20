package com.beg.shuttlespot.core.config

import com.beg.shuttlespot.BuildConfig

object SupabaseConfig {
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    fun requireConfigured() {
        check(isConfigured) { "Supabase is not configured. Add values to local.properties; never commit them." }
    }
}
