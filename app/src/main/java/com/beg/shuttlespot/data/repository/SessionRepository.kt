package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.AuthActionResult
import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.SignUpResult
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSession(): Flow<AppSession?>

    suspend fun restoreSession()

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
        phoneNumber: String,
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
