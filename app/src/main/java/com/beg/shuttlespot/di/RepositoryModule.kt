package com.beg.shuttlespot.di

import com.beg.shuttlespot.data.repository.InMemoryRouteRepository
import com.beg.shuttlespot.data.repository.InMemorySessionRepository
import com.beg.shuttlespot.data.repository.InMemoryPassengerTripRepository
import com.beg.shuttlespot.data.repository.InMemoryDriverTripRepository
import com.beg.shuttlespot.data.repository.SessionRepository
import com.beg.shuttlespot.data.repository.PassengerTripRepository
import com.beg.shuttlespot.data.repository.DriverTripRepository
import com.beg.shuttlespot.data.repository.RouteRepository
import com.beg.shuttlespot.data.location.LocationPublisher
import com.beg.shuttlespot.data.location.NoOpLocationPublisher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module // marks this class as a Hilt configuration file.
@InstallIn(SingletonComponent::class) //Installs its bindings in the app-wide Hilt container.
abstract class RepositoryModule {
    @Binds //When this interface is requested, use this implementation
    @Singleton //Hilt creates only one shared repository instance.
    abstract fun bindRouteRepository(
        inMemoryRouteRepository: InMemoryRouteRepository
    ): RouteRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(implementation: InMemorySessionRepository): SessionRepository

    @Binds @Singleton
    abstract fun bindPassengerTripRepository(implementation: InMemoryPassengerTripRepository): PassengerTripRepository

    @Binds @Singleton
    abstract fun bindDriverTripRepository(implementation: InMemoryDriverTripRepository): DriverTripRepository

    @Binds @Singleton
    abstract fun bindLocationPublisher(implementation: NoOpLocationPublisher): LocationPublisher
}
