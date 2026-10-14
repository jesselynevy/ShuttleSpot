package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.Route
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.RouteSelection
import com.beg.shuttlespot.core.model.Stop
import com.beg.shuttlespot.core.model.TimetableEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton //object u create once and use it everywhere
class InMemoryRouteRepository @Inject constructor(): RouteRepository{
    private val routes = listOf(
        Route(id = "bsd_umn", name = "Pasar Modern BSD ke UMN"),
        Route(id = "gading_serpong_loop", name = "Rute Keliling Gading Serpong"),
        Route(id = "grogol_umn", name = "Grogol ke UMN"),
        Route(id = "umn_grogol", name = "UMN ke Grogol"),
        Route(id = "tangerang_city_umn_mnp", name = "Tangerang City ke UMN/MNP"),
        Route(id = "umn_tangerang_city", name = "UMN ke Tangerang City"),
        Route(id = "cisauk_umn", name = "Intermoda Cisauk ke UMN"),
        Route(id = "umn_cisauk", name = "UMN ke Intermoda Cisauk")
    )

    private val routeDetails = listOf(
        RouteDetails(
            route = routes[0],
            stops = listOf(
                Stop("bsd_umn_1", "bsd_umn", "Halte Pasar Modern BSD", 1, -6.3045281, 106.6848223, scheduledTime = "06.15"),
                Stop("bsd_umn_2", "bsd_umn", "Halte Seberang Telkom", 2, -6.277908133873632, 106.66122527545618),
                Stop("bsd_umn_3", "bsd_umn", "Halte depan Mall WTC Villa Melati Mas", 3, -6.271424137283743, 106.6554922228125),
                Stop("bsd_umn_4", "bsd_umn", "Halte Assobirin", 4, -6.258629712030424, 106.65182741364757),
                Stop("bsd_umn_5", "bsd_umn", "Halte Bundaran Alam Sutera", 5, -6.24412, 106.64886),
                Stop("bsd_umn_6", "bsd_umn", "Gerbang Gading Serpong", 6, -6.23077, 106.64136, scheduledTime = "07.00"),
                Stop("bsd_umn_7", "bsd_umn", "Paramount", 7, -6.2307640413527325, 106.64040084798098),
                Stop("bsd_umn_8", "bsd_umn", "Dormitory", 8, -6.251652290980524, 106.62814256878931),
                Stop("bsd_umn_9", "bsd_umn", "UMN", 9, -6.2567379, 106.6183029, scheduledTime = "07.30"),
            ),
            timetableEntries = listOf(
                entry("Pagi", "06.15", "Halte Pasar Modern BSD – Halte Seberang Telkom – Halte depan Mall WTC Villa Melati Mas – Halte Assobirin – Halte Bundaran Alam Sutera"),
                entry("Pagi", "07.00", "Gerbang Gading Serpong – Paramount"), entry("Pagi", "07.10", "Dormitory – UMN"),
                entry("Pagi", "07.20", "Dormitory"), entry("Pagi", "07.30", "Tiba UMN")
            ),
            path = RoutePaths.BSD_UMN,
        ),
        detail(routes[1], listOf("Kampus UMN", "SMS – Bundaran", "McDonald GS", "Ruko Boulevard", "Dormitory UMN", "Assobirin", "Bundaran Alam Sutera", "Villa Melati Mas", "BSD Plaza", "Halte Pasar Modern BSD"), listOf(
            entry("Rute keliling", "09.15", "Kampus UMN"), entry("Rute keliling", "10.40", "SMS – Bundaran"),
            entry("Rute keliling", "12.15", "McDonald GS"), entry("Rute keliling", "14.15", "Ruko Boulevard – Dormitory UMN – Kampus UMN"),
            entry("Rute keliling", "17.30", "UMN – Dormitory – Assobirin – Bundaran Alam Sutera – Villa Melati Mas – BSD Plaza – Halte Pasar Modern BSD")
        ), trafficNotice),
        detail(routes[2], listOf("Grogol", "Green Garden Pesing", "Halte Pasar Pisang/Bojong Cengkareng", "Gerbang Tol Puri Kembangan", "Apartemen Metro Garden", "Rest Area KM 13,5", "UMN"), listOf(
            entry("Pagi", "05.25–05.47", "Grogol – Green Garden Pesing"), entry("Pagi", "05.47–05.54", "Green Garden Pesing – Halte Pasar Pisang/Bojong Cengkareng"),
            entry("Pagi", "05.54–06.07", "Halte Pasar Pisang/Bojong Cengkareng – Gerbang Tol Puri Kembangan"), entry("Pagi", "06.07–06.17", "Gerbang Tol Puri Kembangan – Apartemen Metro Garden"),
            entry("Pagi", "06.17–06.27", "Apartemen Metro Garden – Rest Area KM 13,5"), entry("Pagi", "06.27–07.00", "Rest Area KM 13,5 – UMN")
        )),
        detail(routes[3], listOf("UMN", "Rest Area Karang Tengah KM 14", "Apartemen Metro Garden", "Gerbang Tol Puri Kembangan", "Halte Pasar Pisang/Bojong Cengkareng", "Green Garden Pesing", "Grogol"), listOf(
            entry("Sore", "17.15–18.15", "UMN – Rest Area Karang Tengah KM 14 – Apartemen Metro Garden – Gerbang Tol Puri Kembangan"),
            entry("Sore", "18.15–18.35", "Gerbang Tol Puri Kembangan – Halte Pasar Pisang/Bojong Cengkareng"), entry("Sore", "18.35–18.55", "Halte Pasar Pisang/Bojong Cengkareng – Green Garden Pesing"),
            entry("Sore", "18.55–19.15", "Green Garden Pesing – Grogol")
        ), trafficNotice),
        detail(routes[4], listOf("Halte Total Depan Tang City", "Depan Transmart Cikokol", "Setos Kebon Nanas", "PT Tiffico", "Gerbang Gading Serpong", "Dormitory", "UMN", "MNP"), listOf(
            entry("Pagi", "06.40", "Halte Total Depan Tang City – Depan Transmart Cikokol – Setos Kebon Nanas – PT Tiffico – Gerbang Gading Serpong"),
            entry("Pagi", "07.15", "Dormitory – UMN – MNP"), entry("Pagi", "07.25", "Dormitory – UMN – MNP"), entry("Pagi", "07.35", "Tiba UMN"), entry("Pagi", "07.40", "Tiba MNP")
        )),
        detail(routes[5], listOf("UMN", "Dormitory", "Gerbang Gading Serpong", "Halte Seberang Transmart Cikokol", "Halte Total Depan Tang City"), listOf(
            entry("Siang", "13.00", "UMN"), entry("Siang", "13.15", "Dormitory – Gerbang Gading Serpong – Halte Seberang Transmart Cikokol – Halte Total Depan Tang City"),
            entry("Sore", "16.00", "Dormitory"), entry("Sore", "16.30", "Tiba UMN"), entry("Sore", "17.30", "UMN – Dormitory – Gerbang Gading Serpong – Halte Seberang Transmart Cikokol – Halte Total Depan Tang City")
        ), trafficNotice),
        detail(routes[6], listOf("Intermoda Cisauk", "UMN"), listOf(entry("Pagi", "07.00", "Intermoda Cisauk – UMN"))),
        detail(routes[7], listOf("UMN", "Intermoda Cisauk"), listOf(entry("Sore", "17.45", "UMN – Intermoda Cisauk")), trafficNotice)
    )

    private fun detail(route: Route, stopNames: List<String>, timetableEntries: List<TimetableEntry>, serviceNotice: String? = null) =
        RouteDetails(route, stopNames.mapIndexed { index, name -> Stop("${route.id}_${index + 1}", route.id, name, index + 1) }, timetableEntries, serviceNotice)

    private fun entry(period: String, time: String, description: String) = TimetableEntry(period, time, description)

    private companion object {
        const val trafficNotice = "Waktu tiba di titik drop-off dapat berubah sesuai kondisi kepadatan lalu lintas."
    }

    private val routeState = MutableStateFlow(routes)
    private val selectionState = MutableStateFlow<RouteSelection?>(null)

    override fun observeRoutes(): Flow<List<Route>> = routeState.asStateFlow()

    override fun observeRouteDetails(routeId: String): Flow<RouteDetails?> {
        return MutableStateFlow(routeDetails.find{it.route.id == routeId})
    }

    override fun observePrimarySelection(): Flow<RouteSelection?> {
        return selectionState.asStateFlow()
    }

    override suspend fun refreshRoutes() = Unit

    override suspend fun savePrimaryRoute(
        routeId: String,
        pickupStopId: String,
        destinationStopId: String
    ) {
        val details = routeDetails.firstOrNull { it.route.id == routeId }
            ?: throw IllegalArgumentException("Route tidak ditemukan.")
        val pickup = details.stops.firstOrNull { it.id == pickupStopId }
            ?: throw IllegalArgumentException("Halte jemput tidak ditemukan.")
        val destination = details.stops.firstOrNull { it.id == destinationStopId }
            ?: throw IllegalArgumentException("Tujuan tidak ditemukan.")
        require(pickup.sequence < destination.sequence) { "Tujuan harus berada setelah halte jemput." }
        selectionState.value = RouteSelection(
            routeId = routeId,
            pickupStopId = pickupStopId,
            destinationStopId = destinationStopId
        )
    }
}

/*
MutableStateFlow = private writable state.
StateFlow = public read-only state.
asStateFlow() = safely expose the read-only version.
*/
