package com.beg.shuttlespot.feature.theme

import androidx.lifecycle.ViewModel
import com.beg.shuttlespot.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean?> = themeRepository.isDarkMode

    fun setDarkMode(enabled: Boolean) {
        themeRepository.setDarkMode(enabled)
    }

    fun resetToSystem() {
        themeRepository.setDarkMode(null)
    }
}
