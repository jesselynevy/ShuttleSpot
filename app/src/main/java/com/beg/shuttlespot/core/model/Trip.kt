package com.beg.shuttlespot.core.model

// Overview: Defines trip for the core model area of ShuttleSpot.

enum class TripStatus { NO_ACTIVE_BUS, EN_ROUTE, AT_PICKUP, WAITING_AT_OTHERSTOP, MISSED, RIDING, COMPLETED, NOT_STARTED }

// Defines the live location type.
data class LiveLocation(
    val latitude: Double,
    val longitude: Double,
    val capturedAtEpochMillis: Long
)

// Defines the eta estimate type.
data class EtaEstimate(
    val minutes: Int?,
    val isStale: Boolean = false
)

// Defines the active trip snapshot type.
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

// Defines the boarding intent status type.
enum class BoardingIntentStatus { NONE, PENDING_SYNC, CONFIRMED, CANCELLED, UNAVAILABLE }

// Defines the boarding intent type.
data class BoardingIntent(
    val tripId: String,
    val stopId: String,
    val status: BoardingIntentStatus,
    val idempotencyKey: String
)

enum class TripLifecycle { NOT_STARTED, ACTIVE, COMPLETED }

data class DriverTripSnapshot(
    val tripId: String,
    val routeId: String,
    val vehicleLabel: String,
    val status: TripLifecycle,
    val currentStopName: String?,
    val nextStopName: String?,
    val currentStopSequence: Int,
    val totalStops: Int,
    val waitingPassengerCount: Int
)

