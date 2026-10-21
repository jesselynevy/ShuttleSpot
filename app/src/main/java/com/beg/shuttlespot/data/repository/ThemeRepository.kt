package com.beg.shuttlespot.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface ThemeRepository {
    val isDarkMode: StateFlow<Boolean?>
    fun setDarkMode(enabled: Boolean?)
}

@Singleton
class AppThemeRepository @Inject constructor(
    @ApplicationContext context: Context
) : ThemeRepository {
    private val prefs = context.getSharedPreferences("shuttlespot_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (prefs.contains("is_dark_mode")) prefs.getBoolean("is_dark_mode", false) else null
    )
    override val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    override fun setDarkMode(enabled: Boolean?) {
        _isDarkMode.value = enabled
        if (enabled != null) {
            prefs.edit().putBoolean("is_dark_mode", enabled).apply()
        } else {
            prefs.edit().remove("is_dark_mode").apply()
        }
    }
}
