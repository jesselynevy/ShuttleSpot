package com.beg.shuttlespot.di

// Overview: Defines repository module for the shuttlespot di area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.BuildConfig
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
import com.beg.shuttlespot.data.demo.DemoRouteDataSource
import com.beg.shuttlespot.data.demo.StaticDemoRouteDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
// Dependencies used by this file.
import javax.inject.Singleton
import com.beg.shuttlespot.data.remote.SupabaseRouteDataSource
import com.beg.shuttlespot.data.remote.SupabaseRouteDataSourceImpl
import com.beg.shuttlespot.data.remote.SupabaseAuthDataSource
import com.beg.shuttlespot.data.remote.SupabaseAuthDataSourceImpl
import com.beg.shuttlespot.data.remote.PassengerMutationRemote
import com.beg.shuttlespot.data.remote.SupabaseOutboxRemoteDataSource
import com.beg.shuttlespot.data.remote.SupabasePassengerMutationRemote
import com.beg.shuttlespot.data.remote.SupabaseDriverTripRepository
import com.beg.shuttlespot.data.sync.RemoteMutationSink
import com.beg.shuttlespot.data.sync.OutboxSyncScheduler
import com.beg.shuttlespot.data.sync.OutboxSyncTrigger
import com.beg.shuttlespot.data.location.FusedLocationPublisher

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
    abstract fun bindAnnouncementRepository(implementation: InMemoryAnnouncementRepository): AnnouncementRepository

    @Binds @Singleton
    abstract fun bindNotificationRepository(implementation: RoomNotificationRepository): NotificationRepository

    @Binds @Singleton
    abstract fun bindThemeRepository(implementation: AppThemeRepository): ThemeRepository

    @Binds @Singleton
    abstract fun bindSupabaseRouteDataSource(implementation: SupabaseRouteDataSourceImpl): SupabaseRouteDataSource

    @Binds @Singleton
    abstract fun bindSupabaseAuthDataSource(implementation: SupabaseAuthDataSourceImpl): SupabaseAuthDataSource

    @Binds @Singleton
    abstract fun bindPassengerMutationRemote(
        implementation: SupabasePassengerMutationRemote,
    ): PassengerMutationRemote

    @Binds @Singleton
    abstract fun bindRemoteMutationSink(
        implementation: SupabaseOutboxRemoteDataSource,
    ): RemoteMutationSink

    @Binds @Singleton
    abstract fun bindOutboxSyncTrigger(
        implementation: OutboxSyncScheduler,
    ): OutboxSyncTrigger

    companion object {
        @Provides
        @Singleton
        fun provideDriverTripRepository(
            demo: InMemoryDriverTripRepository,
            remote: SupabaseDriverTripRepository,
        ): DriverTripRepository = if (BuildConfig.USE_DEMO_DATA) demo else remote

        @Provides
        @Singleton
        fun provideLocationPublisher(
            demo: NoOpLocationPublisher,
            real: FusedLocationPublisher,
        ): LocationPublisher = if (BuildConfig.USE_DEMO_DATA) demo else real
    }
}
