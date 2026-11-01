package com.beg.shuttlespot.data.remote

// Overview: Defines supabase data sources for the data remote area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.ActiveTripSnapshot
import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.BoardingIntent
import com.beg.shuttlespot.core.model.RouteDetails
import kotlinx.coroutines.flow.Flow

/**
 * Network boundary only. Implementations may use the Supabase SDK, but callers
 * must never expose SDK DTOs outside this package.
 */
interface SupabaseRouteDataSource {
    // Prepares or returns fetch routes.
    suspend fun fetchRoutes(): List<RouteDetails>
}

// Defines the supabase trip data source type.
interface SupabaseTripDataSource {
    // Prepares or returns observe active trip.
    fun observeActiveTrip(routeId: String): Flow<ActiveTripSnapshot?>
    // Handles the submit boarding intent responsibility.
    suspend fun submitBoardingIntent(profileId: String, tripId: String, stopId: String, idempotencyKey: String): BoardingIntent
    // Handles the cancel boarding intent responsibility.
    suspend fun cancelBoardingIntent(profileId: String, tripId: String, stopId: String)
}

// Defines the supabase announcement data source type.
interface SupabaseAnnouncementDataSource {
    // Prepares or returns observe announcements.
    fun observeAnnouncements(routeId: String?): Flow<List<Announcement>>
}

// Defines the supabase auth data source type.
interface SupabaseAuthDataSource {
    // Prepares or returns observe session.
    fun observeSession(): Flow<AppSession?>
    // Handles the sign in passenger responsibility.
    suspend fun signInPassenger(email: String, password: String): AppSession
    // Handles the sign in driver responsibility.
    suspend fun signInDriver(phoneE164: String, password: String): AppSession
    // Handles the sign out responsibility.
    suspend fun signOut()
}
