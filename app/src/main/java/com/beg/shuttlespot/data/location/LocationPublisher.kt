package com.beg.shuttlespot.data.location

// Overview: Defines location publisher for the data location area of ShuttleSpot.

interface LocationPublisher {
    // Handles the start action.
    suspend fun start(tripId: String)
    // Handles the stop responsibility.
    suspend fun stop()
}
