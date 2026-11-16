package com.beg.shuttlespot.data.remote

import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.AuthActionResult
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.SignUpResult
import com.beg.shuttlespot.core.model.UserRole
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

private const val CONFIRM_REDIRECT_URL =
    "shuttlespot://auth/callback"

private const val PASSWORD_RESET_REDIRECT_URL =
    "shuttlespot://auth/reset-password"

@Singleton
class SupabaseAuthDataSourceImpl @Inject constructor(
    private val clientProvider: Provider<SupabaseClient>,
) : SupabaseAuthDataSource {

    private val client: SupabaseClient
        get() = clientProvider.get()

    override fun observeSession(): Flow<AppSession?> =
        client.auth.sessionStatus.map { status ->
            when (status) {
                SessionStatus.Initializing -> null

                is SessionStatus.Authenticated -> {
                    val user = status.session.user
                        ?: return@map null

                    loadAppSession(user)
                }

                is SessionStatus.NotAuthenticated -> null

                is SessionStatus.RefreshFailure -> null
            }
        }

    override suspend fun currentSession(): AppSession? {
        val user = client.auth.currentSessionOrNull()?.user
            ?: return null

        return loadAppSession(user)
    }

    override suspend fun signUpPassenger(
        displayName: String,
        email: String,
        password: String,
    ): SignUpResult {
        val normalizedEmail = email.trim().lowercase()

        if (!isStudentEmail(normalizedEmail)) {
            return SignUpResult.Rejected(
                "Gunakan email mahasiswa @student.umn.ac.id.",
            )
        }

        if (displayName.isBlank()) {
            return SignUpResult.Rejected("Nama tidak boleh kosong.")
        }

        if (password.length < 8) {
            return SignUpResult.Rejected(
                "Password minimal 8 karakter.",
            )
        }

        if (!password.hasLetterAndDigit()) {
            return SignUpResult.Rejected(PASSWORD_COMPLEXITY_MESSAGE)
        }

        return runCatching {
            val user = client.auth.signUpWith(
                provider = Email,
                redirectUrl = CONFIRM_REDIRECT_URL,
            ) {
                this.email = normalizedEmail
                this.password = password

                data = buildJsonObject {
                    put("display_name", displayName.trim())
                }
            }

            if (user.isObfuscatedExistingUser()) {
                SignUpResult.AlreadyRegistered
            } else {
                SignUpResult.ConfirmationRequired(normalizedEmail)
            }
        }.getOrElse { error ->
            if (error.isAlreadyRegisteredAuthError()) {
                SignUpResult.AlreadyRegistered
            } else {
                SignUpResult.Rejected(error.toSafeAuthMessage())
            }
        }
    }

    override suspend fun signInPassenger(
        email: String,
        password: String,
    ): SignInResult {
        val normalizedEmail = email.trim().lowercase()

        if (!isStudentEmail(normalizedEmail)) {
            return SignInResult.Rejected(
                "Gunakan email mahasiswa @student.umn.ac.id.",
            )
        }

        return runCatching {
            client.auth.signInWith(Email) {
                this.email = normalizedEmail
                this.password = password
            }

            val session = currentSession()
                ?: error("Profil pengguna belum tersedia.")

            SignInResult.Success(session)
        }.getOrElse { error ->
            SignInResult.Rejected(error.toSafeAuthMessage())
        }
    }

    override suspend fun resendConfirmation(
        email: String,
    ): AuthActionResult {
        val normalizedEmail = email.trim().lowercase()

        if (!isStudentEmail(normalizedEmail)) {
            return AuthActionResult.Rejected(
                "Gunakan email mahasiswa @student.umn.ac.id.",
            )
        }

        return runCatching {
            client.auth.resendEmail(
                type = OtpType.Email.SIGNUP,
                email = normalizedEmail,
            )

            AuthActionResult.Success
        }.getOrElse { error ->
            AuthActionResult.Rejected(error.toSafeAuthMessage())
        }
    }

    override suspend fun sendPasswordReset(
        email: String,
    ): AuthActionResult {
        val normalizedEmail = email.trim().lowercase()

        if (!isStudentEmail(normalizedEmail)) {
            return AuthActionResult.Rejected(
                "Gunakan email mahasiswa @student.umn.ac.id.",
            )
        }

        return runCatching {
            client.auth.resetPasswordForEmail(
                email = normalizedEmail,
                redirectUrl = PASSWORD_RESET_REDIRECT_URL,
            )

            AuthActionResult.Success
        }.getOrElse { error ->
            AuthActionResult.Rejected(error.toSafeAuthMessage())
        }
    }

    override suspend fun updatePassword(
        newPassword: String,
    ): AuthActionResult {
        if (newPassword.length < 8) {
            return AuthActionResult.Rejected(
                "Password minimal 8 karakter.",
            )
        }

        if (!newPassword.hasLetterAndDigit()) {
            return AuthActionResult.Rejected(PASSWORD_COMPLEXITY_MESSAGE)
        }

        return runCatching {
            client.auth.updateUser {
                password = newPassword
            }

            AuthActionResult.Success
        }.getOrElse { error ->
            AuthActionResult.Rejected(error.toSafeAuthMessage())
        }
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }

    private suspend fun loadAppSession(
        user: UserInfo,
    ): AppSession {
        val profile = client
            .from("profiles")
            .select(
                columns = Columns.list(
                    "id",
                    "role",
                    "display_name",
                    "email",
                    "phone_e164",
                ),
            ) {
                filter {
                    eq("id", user.id)
                }
            }
            .decodeSingle<RemoteProfileDto>()

        val role = when (profile.role.lowercase()) {
            "passenger" -> UserRole.PASSENGER
            "driver" -> UserRole.DRIVER
            else -> error("Role ${profile.role} tidak didukung aplikasi.")
        }

        return AppSession(
            profileId = profile.id,
            role = role,
            displayName = profile.displayName,
            contact = profile.email ?: profile.phoneE164.orEmpty(),
        )
    }

    private fun isStudentEmail(email: String): Boolean {
        val parts = email.split("@")

        return parts.size == 2 &&
                parts[0].isNotBlank() &&
                parts[1] == "student.umn.ac.id"
    }

    private fun String.hasLetterAndDigit(): Boolean =
        any(Char::isLetter) && any(Char::isDigit)

}

internal fun UserInfo?.isObfuscatedExistingUser(): Boolean =
    this?.identities?.isEmpty() == true
