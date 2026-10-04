package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.SignInResult
import com.beg.shuttlespot.core.model.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemorySessionRepositoryTest {
    private val repository = InMemorySessionRepository()

    @Test fun passenger_email_must_use_student_domain() = runTest {
        assertTrue(repository.signInPassenger("student@gmail.com") is SignInResult.Rejected)
    }

    @Test fun registered_driver_phone_creates_driver_session() = runTest {
        val result = repository.signInDriver("081234567890")
        assertTrue(result is SignInResult.Success)
        assertEquals(UserRole.DRIVER, (result as SignInResult.Success).session.role)
    }

    @Test fun unknown_driver_phone_is_rejected() = runTest {
        assertTrue(repository.signInDriver("081111111111") is SignInResult.Rejected)
    }
}
