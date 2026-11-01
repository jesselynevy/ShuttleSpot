package com.beg.shuttlespot.data.repository

// Overview: Defines persisted session repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.data.local.CachedSessionEntity
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local session source used by the app. It deliberately delegates demo credential
 * checking to the demo adapter, but persists the resulting non-secret session so
 * navigation can restore after process recreation. A Supabase auth adapter can
 * replace the credential check without changing UI callers.
 */
@Singleton
// Defines the persisted session repository type.
class PersistedSessionRepository @Inject constructor(
    private val dao: ShuttleSpotDao,
    private val demo: InMemorySessionRepository,
) : SessionRepository {
    // Implements framework behavior for observe session.
    override fun observeSession(): Flow<AppSession?> = dao.observeSession().map { stored ->
        stored?.let {
            AppSession(it.profileId, UserRole.valueOf(it.role), it.displayName, it.contact)
        }
    }

    // Implements framework behavior for sign in passenger.
    override suspend fun signInPassenger(email: String, password: String): SignInResult =
        persist(demo.signInPassenger(email, password))

    // Implements framework behavior for sign in driver.
    override suspend fun signInDriver(phoneNumber: String): SignInResult =
        persist(demo.signInDriver(phoneNumber))

    // Implements framework behavior for sign out.
    override suspend fun signOut() = dao.clearSession()

    // Handles the persist responsibility.
    private suspend fun persist(result: SignInResult): SignInResult {
        if (result is SignInResult.Success) {
            val session = result.session
            dao.saveSession(
                CachedSessionEntity(
                    profileId = session.profileId,
                    role = session.role.name,
                    displayName = session.displayName,
                    contact = session.contact,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                )
            )
        }
        return result
    }
}
