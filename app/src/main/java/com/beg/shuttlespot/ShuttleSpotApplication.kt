package com.beg.shuttlespot

// Overview: Defines shuttle spot application for the shuttlespot area of ShuttleSpot.

// Dependencies used by this file.
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.beg.shuttlespot.data.sync.OutboxSyncScheduler
import javax.inject.Inject

@HiltAndroidApp
// Defines the shuttle spot application type.
class ShuttleSpotApplication : Application(){
    @Inject lateinit var outboxSyncScheduler: OutboxSyncScheduler

    // Implements framework behavior for on create.
    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.USE_DEMO_DATA) outboxSyncScheduler.enqueue()
    }
}

/*
The outbox is for actions performed while offline. For example:
Passenger confirms boarding with no internet
  → app saves that request in Room’s OutboxEntity table
  → internet returns
  → WorkManager runs OutboxSyncWorker
  → worker sends queued request to Supabase
  → successful request is removed from the outbox
*/
