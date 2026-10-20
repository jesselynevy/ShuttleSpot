package com.beg.shuttlespot.core.common

// one shared UI-state type
sealed interface UiState<out T>{
    data object Loading : UiState<Nothing>
    data class Success<T>(val data:T): UiState<T>
    data class Error(val message: String): UiState<Nothing>
}