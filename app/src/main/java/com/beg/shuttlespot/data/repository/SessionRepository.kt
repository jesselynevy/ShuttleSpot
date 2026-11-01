package com.beg.shuttlespot.data.repository

// Overview: Defines session repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.SignInResult
import kotlinx.coroutines.flow.Flow

// Defines the session repository type.
interface SessionRepository {
    // Prepares or returns observe session.
    fun observeSession(): Flow<AppSession?>
    // Handles the sign in passenger responsibility.
    suspend fun signInPassenger(email: String, password: String): SignInResult
    // Handles the sign in driver responsibility.
    suspend fun signInDriver(phoneNumber: String): SignInResult
    // Handles the sign out responsibility.
    suspend fun signOut()
}
