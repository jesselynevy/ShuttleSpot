package com.beg.shuttlespot.data.remote

// Overview: Defines supabase data sources for the data remote area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.AuthActionResult
import com.beg.shuttlespot.core.model.RouteDetails
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.SignUpResult
import kotlinx.coroutines.flow.Flow

/**
 * Network boundary only. Implementations may use the Supabase SDK, but callers
 * must never expose SDK DTOs outside this package.
 */
interface SupabaseRouteDataSource {
    // Prepares or returns fetch routes.
    suspend fun fetchRoutes(): List<RouteDetails>
}

// Defines the supabase announcement data source type.
interface SupabaseAnnouncementDataSource {
    // Prepares or returns observe announcements.
    fun observeAnnouncements(routeId: String?): Flow<List<Announcement>>
}

// Defines the supabase auth data source type.
interface SupabaseAuthDataSource {
    fun observeSession(): Flow<AppSession?>
    suspend fun currentSession(): AppSession?
    suspend fun signUpPassenger(
        displayName: String,
        email: String,
        password: String,
    ): SignUpResult

    suspend fun signInPassenger(
        email: String,
        password: String,
    ): SignInResult

    suspend fun signInDriver(
        phone: String,
        password: String,
    ): SignInResult

    suspend fun resendConfirmation(
        email: String,
    ): AuthActionResult

    suspend fun sendPasswordReset(
        email: String,
    ): AuthActionResult

    suspend fun updatePassword(
        newPassword: String,
    ): AuthActionResult

    suspend fun updateDriverPassword(
        currentPassword: String,
        newPassword: String,
    ): AuthActionResult

    suspend fun signOut()
}
