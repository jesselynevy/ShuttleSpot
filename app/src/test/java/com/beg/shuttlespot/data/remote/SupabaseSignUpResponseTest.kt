package com.beg.shuttlespot.data.remote

import io.github.jan.supabase.auth.user.UserInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SupabaseSignUpResponseTest {

    @Test
    fun `empty identities identify an obfuscated existing user`() {
        val user = UserInfo(
            aud = "authenticated",
            id = "obfuscated-user-id",
            identities = emptyList(),
        )

        assertTrue(user.isObfuscatedExistingUser())
    }

    @Test
    fun `missing user or identities are not treated as an existing account`() {
        val user = UserInfo(
            aud = "authenticated",
            id = "new-user-id",
            identities = null,
        )

        assertFalse(user.isObfuscatedExistingUser())
        assertFalse(null.isObfuscatedExistingUser())
    }
}
