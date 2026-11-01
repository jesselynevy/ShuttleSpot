package com.beg.shuttlespot.data.repository

// Overview: Defines persisted route repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.common.Validation
import com.beg.shuttlespot.core.model.GeoPoint
import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.RouteSelection
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.core.model.TimetableEntry
import com.beg.shuttlespot.data.demo.DemoRouteDataSource
import com.beg.shuttlespot.data.local.CachedRouteEntity
import com.beg.shuttlespot.data.local.CachedRoutePointEntity
import com.beg.shuttlespot.data.local.CachedStopEntity
import com.beg.shuttlespot.data.local.CachedTimetableEntryEntity
import com.beg.shuttlespot.data.local.OutboxEntity
import com.beg.shuttlespot.data.local.OutboxOperation
import com.beg.shuttlespot.data.local.PrimarySelectionEntity
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Room is the read model; demo data only refreshes that cache until Supabase is installed. */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
// Defines the persisted route repository type.
class PersistedRouteRepository @Inject constructor(
    private val demo: DemoRouteDataSource,
    private val sessions: SessionRepository,
    private val dao: ShuttleSpotDao,
) : RouteRepository {
    // Implements framework behavior for observe routes.
    override fun observeRoutes(): Flow<List<Route>> = dao.observeRoutes()
        .map { routes -> routes.map { Route(it.id, it.name) } }

    // Implements framework behavior for observe route details.
    override fun observeRouteDetails(routeId: String): Flow<RouteDetails?> = dao.observeRoute(routeId)
        .flatMapLatest { route ->
            if (route == null) flowOf(null)
            else combine(
                dao.observeStops(routeId),
                dao.observeTimetableEntries(routeId),
                dao.observeRoutePoints(routeId),
            ) { stops, timetable, points ->
                RouteDetails(
                    route = Route(route.id, route.name),
                    stops = stops.map { it.toDomain() },
                    timetableEntries = timetable.map { TimetableEntry(it.period, it.time, it.description) },
                    serviceNotice = route.serviceNotice,
                    path = points.map { GeoPoint(it.latitude, it.longitude) },
                )
            }
        }

    // Implements framework behavior for observe primary selection.
    override fun observePrimarySelection(): Flow<RouteSelection?> = sessions.observeSession()
        .flatMapLatest { session ->
            if (session == null) flowOf(null)
            else dao.observeSelection(session.profileId).map { it?.toDomain() }
        }

    // Implements framework behavior for refresh routes.
    override suspend fun refreshRoutes() {
        val fetchedAt = System.currentTimeMillis()
        val details = demo.getRoutes()
        dao.upsertRoutes(details.map {
            CachedRouteEntity(it.route.id, it.route.name, it.serviceNotice, fetchedAt)
        })
        dao.upsertStops(details.flatMap { detail -> detail.stops.map { it.toEntity() } })
        dao.upsertTimetableEntries(details.flatMap { detail ->
            detail.timetableEntries.mapIndexed { index, entry ->
                CachedTimetableEntryEntity(detail.route.id, index, entry.period, entry.time, entry.description)
            }
        })
        dao.upsertRoutePoints(details.flatMap { detail ->
            detail.path.mapIndexed { index, point ->
                CachedRoutePointEntity(detail.route.id, index, point.latitude, point.longitude)
            }
        })
    }

    // Implements framework behavior for save primary route.
    override suspend fun savePrimaryRoute(routeId: String, pickupStopId: String, destinationStopId: String) {
        val details = requireNotNull(observeRouteDetails(routeId).first()) { "Route tidak ditemukan." }
        val pickup = requireNotNull(details.stops.firstOrNull { it.id == pickupStopId }) { "Halte jemput tidak ditemukan." }
        val destination = requireNotNull(details.stops.firstOrNull { it.id == destinationStopId }) { "Tujuan tidak ditemukan." }
        Validation.requireOrderedStops(pickup.sequence, destination.sequence)
        val profileId = requireNotNull(sessions.observeSession().first()?.profileId) { "Sesi diperlukan untuk menyimpan rute." }

        dao.saveSelection(PrimarySelectionEntity(profileId, routeId, pickupStopId, destinationStopId))
        dao.enqueue(
            OutboxEntity(
                idempotencyKey = UUID.randomUUID().toString(),
                operationType = OutboxOperation.SAVE_PRIMARY_SELECTION.name,
                payload = JSONObject()
                    .put("profileId", profileId)
                    .put("routeId", routeId)
                    .put("pickupStopId", pickupStopId)
                    .put("destinationStopId", destinationStopId)
                    .toString(),
                createdAtEpochMillis = System.currentTimeMillis(),
            )
        )
    }

    // Handles the cached stop entity responsibility.
    private fun CachedStopEntity.toDomain() = Stop(id, routeId, name, sequence, latitude, longitude, scheduledTime)
    // Handles the stop responsibility.
    private fun Stop.toEntity() = CachedStopEntity(id, routeId, name, sequence, latitude, longitude, scheduledTime)
    // Handles the primary selection entity responsibility.
    private fun PrimarySelectionEntity.toDomain() = RouteSelection(routeId, pickupStopId, destinationStopId)
}
