package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.BuildConfig
import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.AuthActionResult
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.SignUpResult
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.data.local.CachedSessionEntity
import com.beg.shuttlespot.data.local.ShuttleSpotDao
import com.beg.shuttlespot.data.remote.SupabaseAuthDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PersistedSessionRepository @Inject constructor(
    private val dao: ShuttleSpotDao,
    private val demo: InMemorySessionRepository,
    private val remoteProvider: Provider<SupabaseAuthDataSource>,
) : SessionRepository {

    override fun observeSession(): Flow<AppSession?> =
        dao.observeSession().map { stored ->
            stored?.let {
                AppSession(
                    profileId = it.profileId,
                    role = UserRole.valueOf(it.role),
                    displayName = it.displayName,
                    contact = it.contact,
                    mustChangePassword = it.mustChangePassword,
                )
            }
        }

    override suspend fun restoreSession() {
        if (BuildConfig.USE_DEMO_DATA) return

        restoreRemoteSession(
            loadSession = { remoteProvider.get().currentSession() },
            saveSession = ::saveSession,
            clearSession = dao::clearSession,
        )
    }

    override suspend fun signUpPassenger(
        displayName: String,
        email: String,
        password: String,
    ): SignUpResult {
        if (BuildConfig.USE_DEMO_DATA) {
            return SignUpResult.Rejected(
                "Signup Supabase tidak tersedia pada demo mode.",
            )
        }

        return remoteProvider.get().signUpPassenger(
            displayName = displayName,
            email = email,
            password = password,
        )
    }

    override suspend fun signInPassenger(
        email: String,
        password: String,
    ): SignInResult {
        val result = if (BuildConfig.USE_DEMO_DATA) {
            demo.signInPassenger(email, password)
        } else {
            remoteProvider.get().signInPassenger(email, password)
        }

        return persist(result)
    }

    override suspend fun signInDriver(
        phoneNumber: String,
        password: String,
    ): SignInResult {
        val result = if (BuildConfig.USE_DEMO_DATA) {
            demo.signInDriver(phoneNumber, password)
        } else {
            remoteProvider.get().signInDriver(phoneNumber, password)
        }
        return persist(result)
    }

    override suspend fun resendConfirmation(
        email: String,
    ): AuthActionResult {
        if (BuildConfig.USE_DEMO_DATA) {
            return AuthActionResult.Rejected(
                "Email confirmation tidak tersedia pada demo mode.",
            )
        }

        return remoteProvider.get().resendConfirmation(email)
    }

    override suspend fun sendPasswordReset(
        email: String,
    ): AuthActionResult {
        if (BuildConfig.USE_DEMO_DATA) {
            return AuthActionResult.Rejected(
                "Password reset tidak tersedia pada demo mode.",
            )
        }

        return remoteProvider.get().sendPasswordReset(email)
    }

    override suspend fun updatePassword(
        newPassword: String,
    ): AuthActionResult {
        if (BuildConfig.USE_DEMO_DATA) {
            return AuthActionResult.Rejected(
                "Password update tidak tersedia pada demo mode.",
            )
        }

        return remoteProvider.get().updatePassword(newPassword)
    }

    override suspend fun updateDriverPassword(
        currentPassword: String,
        newPassword: String,
    ): AuthActionResult {
        if (BuildConfig.USE_DEMO_DATA) {
            return demo.updateDriverPassword(currentPassword, newPassword)
        }

        val remote = remoteProvider.get()
        val result = remote.updateDriverPassword(currentPassword, newPassword)
        if (result != AuthActionResult.Success) return result

        return runCatching {
            val refreshed = requireNotNull(remote.currentSession())
            check(!refreshed.mustChangePassword)
            saveSession(refreshed)
            AuthActionResult.Success
        }.getOrElse {
            runCatching { remote.signOut() }
            dao.clearSession()
            AuthActionResult.Rejected(
                "Password berubah, tetapi profil belum dapat dimuat ulang. Silakan masuk kembali.",
            )
        }
    }

    override suspend fun signOut() {
        if (!BuildConfig.USE_DEMO_DATA) {
            runCatching {
                remoteProvider.get().signOut()
            }
        }

        // Local session harus selalu dibersihkan.
        dao.clearSession()
    }

    private suspend fun persist(
        result: SignInResult,
    ): SignInResult {
        if (result is SignInResult.Success) {
            saveSession(result.session)
        }

        return result
    }

    private suspend fun saveSession(
        session: AppSession,
    ) {
        dao.saveSession(
            CachedSessionEntity(
                profileId = session.profileId,
                role = session.role.name,
                displayName = session.displayName,
                contact = session.contact,
                mustChangePassword = session.mustChangePassword,
                updatedAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }
}

internal suspend fun restoreRemoteSession(
    loadSession: suspend () -> AppSession?,
    saveSession: suspend (AppSession) -> Unit,
    clearSession: suspend () -> Unit,
) {
    try {
        val session = loadSession()
        if (session != null) {
            saveSession(session)
        } else {
            clearSession()
        }
    } catch (_: Exception) {
        // A network/profile lookup failure is not proof that the authenticated
        // session expired, so retain the cached session for offline rendering.
    }
}
