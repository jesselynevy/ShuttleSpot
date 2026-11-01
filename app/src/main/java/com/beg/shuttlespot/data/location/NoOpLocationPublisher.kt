package com.beg.shuttlespot.data.location

// Overview: Defines no op location publisher for the data location area of ShuttleSpot.

// Dependencies used by this file.
import javax.inject.Inject
import javax.inject.Singleton

/** Demo adapter. The driver feature replaces this with a foreground-service adapter. */
@Singleton
// Defines the no op location publisher type.
class NoOpLocationPublisher @Inject constructor() : LocationPublisher {
    // Implements framework behavior for start.
    override suspend fun start(tripId: String) = Unit
    // Implements framework behavior for stop.
    override suspend fun stop() = Unit
}
