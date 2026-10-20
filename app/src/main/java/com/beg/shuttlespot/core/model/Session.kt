package com.beg.shuttlespot.core.model

enum class UserRole { PASSENGER, DRIVER }

data class AppSession(
    val profileId: String,
    val role: UserRole,
    val displayName: String,
    val contact: String
)

sealed interface SignInResult {
    data class Success(val session: AppSession) : SignInResult
    data class Rejected(val message: String) : SignInResult
}
