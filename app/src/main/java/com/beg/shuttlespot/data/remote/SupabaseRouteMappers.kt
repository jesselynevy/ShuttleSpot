package com.beg.shuttlespot.data.remote

import com.beg.shuttlespot.core.model.GeoPoint
import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.core.model.TimetableEntry

internal fun buildRouteDetails(
    routes: List<RemoteRouteDto>,
    stops: List<RemoteStopDto>,
    points: List<RemoteRoutePointDto>,
    timetableEntries: List<RemoteTimetableEntryDto>,
): List<RouteDetails> {
    val stopsByRoute = stops.groupBy { it.routeId }
    val pointsByRoute = points.groupBy { it.routeId }
    val timetableByRoute = timetableEntries.groupBy { it.routeId }

    return routes.map { route ->
        RouteDetails(
            route = Route(
                id = route.id,
                name = route.name,
            ),
            stops = stopsByRoute[route.id]
                .orEmpty()
                .sortedBy { it.sequence }
                .map {
                    Stop(
                        id = it.id,
                        routeId = it.routeId,
                        name = it.name,
                        sequence = it.sequence,
                        latitude = it.latitude,
                        longitude = it.longitude,
                    )
                },
            timetableEntries = timetableByRoute[route.id]
                .orEmpty()
                .sortedBy { it.sequence }
                .map {
                    TimetableEntry(
                        period = it.period,
                        time = it.departureTime,
                        description = it.description,
                    )
                },
            serviceNotice = route.serviceNotice,
            path = pointsByRoute[route.id]
                .orEmpty()
                .sortedBy { it.sequence }
                .map {
                    GeoPoint(
                        latitude = it.latitude,
                        longitude = it.longitude,
                    )
                },
        )
    }
}