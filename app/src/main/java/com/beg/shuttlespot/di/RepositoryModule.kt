package com.beg.shuttlespot.di

// Overview: Defines repository module for the shuttlespot di area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.data.location.LocationPublisher
import com.beg.shuttlespot.data.location.NoOpLocationPublisher
import com.beg.shuttlespot.data.repository.AnnouncementRepository
import com.beg.shuttlespot.data.repository.AppThemeRepository
import com.beg.shuttlespot.data.repository.DriverTripRepository
import com.beg.shuttlespot.data.repository.InMemoryAnnouncementRepository
import com.beg.shuttlespot.data.repository.InMemoryDriverTripRepository
import com.beg.shuttlespot.data.repository.RoomPassengerTripRepository
// Dependencies used by this file.
import com.beg.shuttlespot.data.repository.PassengerTripRepository
import com.beg.shuttlespot.data.repository.PersistedSessionRepository
import com.beg.shuttlespot.data.repository.NotificationRepository
import com.beg.shuttlespot.data.repository.RoomNotificationRepository
import com.beg.shuttlespot.data.repository.RouteRepository
import com.beg.shuttlespot.data.repository.PersistedRouteRepository
import com.beg.shuttlespot.data.repository.SessionRepository
import com.beg.shuttlespot.data.repository.ThemeRepository
// Dependencies used by this file.
import com.beg.shuttlespot.data.sync.RemoteMutationSink
import com.beg.shuttlespot.data.local.OutboxEntity
import com.beg.shuttlespot.data.demo.DemoRouteDataSource
import com.beg.shuttlespot.data.demo.StaticDemoRouteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
// Dependencies used by this file.
import javax.inject.Singleton

@Module // marks this class as a Hilt configuration file.
@InstallIn(SingletonComponent::class) //Installs its bindings in the app-wide Hilt container.
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindDemoRouteDataSource(
        implementation: StaticDemoRouteDataSource,
    ): DemoRouteDataSource

    @Binds //When this interface is requested, use this implementation
    @Singleton //Hilt creates only one shared repository instance.
    abstract fun bindRouteRepository(
        implementation: PersistedRouteRepository,
    ): RouteRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(implementation: PersistedSessionRepository): SessionRepository

    @Binds @Singleton
    abstract fun bindPassengerTripRepository(implementation: RoomPassengerTripRepository): PassengerTripRepository

    @Binds @Singleton
    abstract fun bindDriverTripRepository(implementation: InMemoryDriverTripRepository): DriverTripRepository

    @Binds @Singleton
    abstract fun bindAnnouncementRepository(implementation: InMemoryAnnouncementRepository): AnnouncementRepository

    @Binds @Singleton
    abstract fun bindNotificationRepository(implementation: RoomNotificationRepository): NotificationRepository

    @Binds @Singleton
    abstract fun bindLocationPublisher(implementation: NoOpLocationPublisher): LocationPublisher

    @Binds @Singleton
    abstract fun bindThemeRepository(implementation: AppThemeRepository): ThemeRepository
}

@Module
@InstallIn(SingletonComponent::class)
// Defines the sync module type.
object SyncModule {
    @dagger.Provides @Singleton
    // Prepares or returns provide remote mutation sink.
    fun provideRemoteMutationSink(): RemoteMutationSink = object : RemoteMutationSink {
        // No production transport is configured yet. The worker is intentionally
        // not scheduled while demo mode is active.
        override suspend fun deliver(operation: OutboxEntity): Boolean = false
    }
}
