package com.beg.shuttlespot.data.location

import javax.inject.Inject
import javax.inject.Singleton

/** Demo adapter. The driver feature replaces this with a foreground-service adapter. */
@Singleton
class NoOpLocationPublisher @Inject constructor() : LocationPublisher {
    override suspend fun start(tripId: String) = Unit
    override suspend fun stop() = Unit
}
