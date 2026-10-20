package com.beg.shuttlespot.feature.passenger.boarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.BoardingIntent
import com.beg.shuttlespot.core.model.BoardingIntentStatus
import com.beg.shuttlespot.data.repository.PassengerTripRepository
import com.beg.shuttlespot.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BoardingIntentUiState(
    val tripId: String? = null,
    val pickupStopId: String? = null,
    val intent: BoardingIntent? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class BoardingIntentViewModel @Inject constructor(
    private val repository: PassengerTripRepository,
    routeRepository: RouteRepository
) : ViewModel() {
    private val submitting = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val idempotencyKey = UUID.randomUUID().toString()

    private val selectionState = routeRepository.observePrimarySelection()
        .flatMapLatest { selection ->
            if (selection == null) return@flatMapLatest flowOf(BoardingIntentUiState())
            repository.observeActiveTrip(selection.routeId).flatMapLatest { trip ->
                if (trip == null) flowOf(BoardingIntentUiState(pickupStopId = selection.pickupStopId))
                else repository.observeBoardingIntent(trip.tripId, selection.pickupStopId)
                    .map { intent -> BoardingIntentUiState(trip.tripId, selection.pickupStopId, intent) }
            }
        }

    val state: StateFlow<BoardingIntentUiState> = combine(
        selectionState,
        submitting,
        error
    ) { selection, isSubmitting, errorMessage ->
        selection.copy(isSubmitting = isSubmitting, errorMessage = errorMessage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BoardingIntentUiState())

    fun confirm() {
        val tripId = state.value.tripId ?: return
        val pickupStopId = state.value.pickupStopId ?: return
        if (state.value.intent?.status == BoardingIntentStatus.CONFIRMED || submitting.value) return
        viewModelScope.launch {
            submitting.value = true
            error.value = null
            runCatching {
                repository.submitBoardingIntent(tripId, pickupStopId, idempotencyKey)
            }.onFailure { error.value = "Konfirmasi belum dapat disimpan. Coba lagi." }
            submitting.value = false
        }
    }

    fun cancel() {
        val tripId = state.value.tripId ?: return
        val pickupStopId = state.value.pickupStopId ?: return
        if (submitting.value) return
        viewModelScope.launch {
            submitting.value = true
            repository.cancelBoardingIntent(tripId, pickupStopId)
            submitting.value = false
        }
    }
}
