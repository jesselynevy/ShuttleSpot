package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.Route
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    fun observeRoutes(): Flow<List<Route>>
    suspend fun refreshRoutes()
    suspend fun savePrimaryRoute(routeId: String, stopId: String)
}