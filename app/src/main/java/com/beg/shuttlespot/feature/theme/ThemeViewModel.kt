package com.beg.shuttlespot.feature.theme

// Overview: Defines theme view model for the feature theme area of ShuttleSpot.

// Dependencies used by this file.
import androidx.lifecycle.ViewModel
import com.beg.shuttlespot.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
// Defines the theme view model type.
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean?> = themeRepository.isDarkMode

    // Handles the set dark mode responsibility.
    fun setDarkMode(enabled: Boolean) {
        themeRepository.setDarkMode(enabled)
    }

    // Handles the reset to system responsibility.
    fun resetToSystem() {
        themeRepository.setDarkMode(null)
    }
}
