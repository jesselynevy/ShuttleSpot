package com.beg.shuttlespot.data.location

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FusedLocationPublisher @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationPublisher {

    override suspend fun start(tripId: String) {
        ShuttleSpotLocationService.start(context, tripId)
    }

    override suspend fun stop() {
        ShuttleSpotLocationService.stop(context)
    }
}