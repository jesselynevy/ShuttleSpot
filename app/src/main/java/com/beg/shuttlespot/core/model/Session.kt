package com.beg.shuttlespot.core.model

// Overview: Defines session for the core model area of ShuttleSpot.

enum class UserRole { PASSENGER, DRIVER }

// Defines the app session type.
data class AppSession(
    val profileId: String,
    val role: UserRole,
    val displayName: String,
    val contact: String
)

// Defines the sign in result type.
sealed interface SignInResult {
    // Defines the success type.
    data class Success(val session: AppSession) : SignInResult
    // Defines the rejected type.
    data class Rejected(val message: String) : SignInResult
}

sealed interface SignUpResult {
    data class ConfirmationRequired(
        val email: String,
    ) : SignUpResult

    data object AlreadyRegistered : SignUpResult

    data class Rejected(
        val message: String,
    ) : SignUpResult
}

sealed interface AuthActionResult {
    data object Success : AuthActionResult

    data class Rejected(
        val message: String,
    ) : AuthActionResult
}
