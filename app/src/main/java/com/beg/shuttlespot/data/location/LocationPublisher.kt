package com.beg.shuttlespot.data.location

interface LocationPublisher {
    suspend fun start(tripId: String)
    suspend fun stop()
}
