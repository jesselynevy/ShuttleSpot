package com.beg.shuttlespot.data.repository

// Overview: Defines in memory session repository for the data repository area of ShuttleSpot.

// Dependencies used by this file.
import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.AuthActionResult
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.SignUpResult
import com.beg.shuttlespot.core.model.UserRole
import com.beg.shuttlespot.core.common.Validation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// Defines the in memory session repository type.
class InMemorySessionRepository @Inject constructor() : SessionRepository {
    private val session = MutableStateFlow<AppSession?>(null)
    private val registeredDrivers = setOf("+6281234567890", "+6289530000000")
    private val demoStudentEmails = setOf(
        "jimmy.donaldson@student.umn.ac.id"
    )
    private val demoStudentPassword = "enterpassword"
    private val demoDriverPassword = "driverpassword1"

    // Implements framework behavior for observe session.
    override fun observeSession(): Flow<AppSession?> = session.asStateFlow()

    override suspend fun restoreSession() = Unit

    override suspend fun signUpPassenger(
        displayName: String,
        email: String,
        password: String,
    ): SignUpResult = SignUpResult.Rejected(
        "Pendaftaran akun tidak tersedia dalam mode demo.",
    )

    // Implements framework behavior for sign in passenger.
    override suspend fun signInPassenger(email: String, password: String): SignInResult {
        val normalizedEmail = email.trim().lowercase()
        if (!Validation.isStudentEmail(normalizedEmail)) {
            return SignInResult.Rejected("Gunakan email dengan domain student.umn.ac.id.")
        }
        if (normalizedEmail !in demoStudentEmails || password != demoStudentPassword) {
            return SignInResult.Rejected("Email atau password SSO tidak sesuai.")
        }
        return AppSession("passenger-demo", UserRole.PASSENGER, "Jimmy Donaldson", normalizedEmail)
            .also { session.value = it }
            .let(SignInResult::Success)
    }

    // Implements framework behavior for sign in driver.
    override suspend fun signInDriver(
        phoneNumber: String,
        password: String,
    ): SignInResult {
        val normalized = Validation.normalizeIndonesianPhone(phoneNumber)
        if (normalized !in registeredDrivers || password != demoDriverPassword) {
            return SignInResult.Rejected("Nomor telepon atau password salah.")
        }
        return AppSession("driver-demo", UserRole.DRIVER, "Pak Wahyu", normalized)
            .also { session.value = it }
            .let(SignInResult::Success)
    }

    override suspend fun resendConfirmation(email: String): AuthActionResult =
        AuthActionResult.Rejected(
            "Konfirmasi email tidak tersedia dalam mode demo.",
        )

    override suspend fun sendPasswordReset(email: String): AuthActionResult =
        AuthActionResult.Rejected(
            "Reset password tidak tersedia dalam mode demo.",
        )

    override suspend fun updatePassword(newPassword: String): AuthActionResult =
        AuthActionResult.Rejected(
            "Pembaruan password tidak tersedia dalam mode demo.",
        )

    override suspend fun updateDriverPassword(
        currentPassword: String,
        newPassword: String,
    ): AuthActionResult {
        if (currentPassword != demoDriverPassword) {
            return AuthActionResult.Rejected("Password sementara salah.")
        }
        session.value = session.value?.copy(mustChangePassword = false)
        return AuthActionResult.Success
    }

    // Implements framework behavior for sign out.
    override suspend fun signOut() { session.value = null }

}
