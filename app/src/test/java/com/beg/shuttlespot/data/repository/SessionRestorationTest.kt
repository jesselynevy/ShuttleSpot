package com.beg.shuttlespot.data.repository

import com.beg.shuttlespot.core.model.AppSession
import com.beg.shuttlespot.core.model.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRestorationTest {

    @Test
    fun `missing remote session clears stale cached session`() = runTest {
        var cleared = false

        restoreRemoteSession(
            loadSession = { null },
            saveSession = { error("No session should be saved") },
            clearSession = { cleared = true },
        )

        assertTrue(cleared)
    }

    @Test
    fun `remote failure preserves cached session for offline use`() = runTest {
        var cleared = false
        var saved = false

        restoreRemoteSession(
            loadSession = { error("Network unavailable") },
            saveSession = { saved = true },
            clearSession = { cleared = true },
        )

        assertFalse(cleared)
        assertFalse(saved)
    }

    @Test
    fun `valid remote session replaces cached session`() = runTest {
        val expected = AppSession(
            profileId = "profile-id",
            role = UserRole.PASSENGER,
            displayName = "Student",
            contact = "student@student.umn.ac.id",
        )
        var actual: AppSession? = null
        var cleared = false

        restoreRemoteSession(
            loadSession = { expected },
            saveSession = { actual = it },
            clearSession = { cleared = true },
        )

        assertEquals(expected, actual)
        assertFalse(cleared)
    }
}
