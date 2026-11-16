package com.beg.shuttlespot.data.remote

import com.beg.shuttlespot.core.model.RouteDetails
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SupabaseRouteDataSourceImpl @Inject constructor(
    private val clientProvider: Provider<SupabaseClient>,
) : SupabaseRouteDataSource {

    override suspend fun fetchRoutes(): List<RouteDetails> {
        val client = clientProvider.get()

        val routes = client.from("routes")
            .select(
                columns = Columns.list("id", "name", "service_notice"),
            ) {
                order(column = "name", order = Order.ASCENDING)
            }
            .decodeList<RemoteRouteDto>()

        val stops = client.from("stops")
            .select(
                columns = Columns.list(
                    "id",
                    "route_id",
                    "name",
                    "sequence",
                    "latitude",
                    "longitude",
                ),
            ) {
                order(column = "sequence", order = Order.ASCENDING)
            }
            .decodeList<RemoteStopDto>()

        val points = client.from("route_geometry_points")
            .select(
                columns = Columns.list(
                    "route_id",
                    "sequence",
                    "latitude",
                    "longitude",
                ),
            ) {
                order(column = "sequence", order = Order.ASCENDING)
            }
            .decodeList<RemoteRoutePointDto>()

        val timetableEntries = client.from("route_timetable_entries")
            .select(
                columns = Columns.list(
                    "route_id",
                    "sequence",
                    "period",
                    "departure_time",
                    "description",
                ),
            ) {
                order(column = "sequence", order = Order.ASCENDING)
            }
            .decodeList<RemoteTimetableEntryDto>()

        return buildRouteDetails(
            routes = routes,
            stops = stops,
            points = points,
            timetableEntries = timetableEntries,
        )
    }
}