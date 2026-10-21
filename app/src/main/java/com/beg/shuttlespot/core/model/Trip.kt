package com.beg.shuttlespot.core.model

enum class TripStatus { NO_ACTIVE_BUS, EN_ROUTE, AT_PICKUP, WAITING_AT_OTHERSTOP, MISSED, RIDING, COMPLETED, NOT_STARTED }

data class LiveLocation(
    val latitude: Double,
    val longitude: Double,
    val capturedAtEpochMillis: Long
)

data class EtaEstimate(
    val minutes: Int?,
    val isStale: Boolean = false
)

data class ActiveTripSnapshot(
    val tripId: String,
    val routeId: String,
    val vehicleLabel: String,
    val status: TripStatus,
    val progress: Float,
    /** Halte yang baru dilewati/ditinggalkan bus pada ruas yang sedang berjalan sekarang. */
    val previousStopName: String? = null,
    val nextStopName: String?,
    val eta: EtaEstimate,
    val lastUpdatedEpochMillis: Long?,
    val location: LiveLocation?
)

enum class BoardingIntentStatus { NONE, PENDING_SYNC, CONFIRMED, CANCELLED, UNAVAILABLE }

data class BoardingIntent(
    val tripId: String,
    val stopId: String,
    val status: BoardingIntentStatus,
    val idempotencyKey: String
)

data class DriverTripSnapshot(
    val tripId: String,
    val routeId: String,
    val vehicleLabel: String,
    val status: TripStatus,
    val currentStopName: String?,
    val nextStopName: String?,
    val currentStopSequence: Int,
    val totalStops: Int,
    val waitingPassengerCount: Int
)
