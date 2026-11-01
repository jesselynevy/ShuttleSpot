package com.beg.shuttlespot.core.common

// Overview: Defines ui state for the core common area of ShuttleSpot.

// one shared UI-state type
sealed interface UiState<out T>{
    // Defines the loading type.
    data object Loading : UiState<Nothing>
    // Defines the success type.
    data class Success<T>(val data:T): UiState<T>
    // Defines the error type.
    data class Error(val message: String): UiState<Nothing>
}
