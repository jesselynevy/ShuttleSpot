package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemorySessionRepository @Inject constructor() : SessionRepository {
    private val session = MutableStateFlow<AppSession?>(null)
    private val registeredDrivers = setOf("+6281234567890", "+6289530000000")

    override fun observeSession(): Flow<AppSession?> = session.asStateFlow()

    override suspend fun signInPassenger(email: String): SignInResult {
        val normalizedEmail = email.trim().lowercase()
        if (!normalizedEmail.matches(Regex("^[^@\\s]+@student\\.umn\\.ac\\.id$"))) {
            return SignInResult.Rejected("Gunakan email mahasiswa @student.umn.ac.id.")
        }
        return AppSession("passenger-demo", UserRole.PASSENGER, normalizedEmail.substringBefore('@'), normalizedEmail)
            .also { session.value = it }
            .let(SignInResult::Success)
    }

    override suspend fun signInDriver(phoneNumber: String): SignInResult {
        val normalized = normalizePhone(phoneNumber)
        if (normalized !in registeredDrivers) {
            return SignInResult.Rejected("Nomor belum terdaftar. Hubungi koordinator shuttle.")
        }
        return AppSession("driver-demo", UserRole.DRIVER, "Pak Wahyu", normalized)
            .also { session.value = it }
            .let(SignInResult::Success)
    }

    override suspend fun signOut() { session.value = null }

    private fun normalizePhone(value: String): String {
        val digits = value.filter(Char::isDigit)
        return when {
            digits.startsWith("62") -> "+$digits"
            digits.startsWith("0") -> "+62${digits.drop(1)}"
            else -> "+$digits"
        }
    }
}
