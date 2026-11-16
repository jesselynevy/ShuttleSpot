package com.beg.shuttlespot.data.sync

// Overview: Defines outbox sync worker for the data sync area of ShuttleSpot.

// Dependencies used by this file.
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Constraints
// Dependencies used by this file.
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@EntryPoint
@InstallIn(SingletonComponent::class)
// Defines the sync worker entry point type.
interface SyncWorkerEntryPoint {
    // Handles the outbox processor responsibility.
    fun outboxProcessor(): OutboxProcessor
}

// Defines the outbox sync worker type.
class OutboxSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    // Implements framework behavior for do work.
    override suspend fun doWork(): Result {
        val processor = EntryPointAccessors.fromApplication(applicationContext, SyncWorkerEntryPoint::class.java)
            .outboxProcessor()
        return if (processor.processAll()) Result.success() else Result.retry()
    }
}

fun interface OutboxSyncTrigger {
    fun enqueue()
}

// Defines the outbox sync scheduler type.
@Singleton
class OutboxSyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : OutboxSyncTrigger {
    // Handles the enqueue responsibility.
    override fun enqueue() {
        val request = OneTimeWorkRequestBuilder<OutboxSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "shuttlespot-outbox-sync"
    }
}
