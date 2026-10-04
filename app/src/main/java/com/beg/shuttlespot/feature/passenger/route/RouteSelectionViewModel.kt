package com.beg.shuttlespot.feature.passenger.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.data.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteSelectionViewModel @Inject constructor(
    private val routeRepository: RouteRepository
): ViewModel(){
    val routes: StateFlow<List<Route>> = routeRepository
        .observeRoutes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun saveDemoSelection(onSaved: () -> Unit) = viewModelScope.launch {
        val route = routes.value.firstOrNull() ?: return@launch
        val details = routeRepository.observeRouteDetails(route.id).stateIn(viewModelScope, SharingStarted.Eagerly, null).value ?: return@launch
        routeRepository.savePrimaryRoute(route.id, details.stops.first().id, details.stops.last().id)
        onSaved()
    }
}
