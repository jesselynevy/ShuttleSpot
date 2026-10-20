package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.SignInResult
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeSession(): Flow<AppSession?>
    suspend fun signInPassenger(email: String, password: String): SignInResult
    suspend fun signInDriver(phoneNumber: String): SignInResult
    suspend fun signOut()
}
