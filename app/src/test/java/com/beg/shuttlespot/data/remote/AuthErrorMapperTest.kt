package com.beg.shuttlespot.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun `weak password error is short and does not expose request details`() {
        val error = IllegalStateException(
            """
            weak_password Password should contain at least one character and one digit
            URL: https://project.example/auth/v1/signup?redirect_to=shuttlespot
            Headers: Authorization=[Bearer <REDACTED>], apikey=[<REDACTED>]
            Http Method: POST
            """.trimIndent(),
        )

        val message = error.toSafeAuthMessage()

        assertEquals("Password should contain character and digits.", message)
        assertFalse(message.contains("https://"))
        assertFalse(message.contains("Headers"))
        assertFalse(message.contains("Bearer"))
    }

    @Test
    fun `unknown auth error never exposes raw exception details`() {
        val error = IllegalStateException(
            "Unexpected failure URL: https://project.example Headers: Bearer <REDACTED>",
        )

        val message = error.toSafeAuthMessage()

        assertEquals("Operasi autentikasi gagal. Silakan coba lagi.", message)
    }

    @Test
    fun `email resend rate limit code has a short retry message`() {
        val error = IllegalStateException(
            "over_email_send_rate_limit URL: https://project.example/auth/v1/resend",
        )

        val message = error.toSafeAuthMessage()

        assertEquals("Terlalu banyak percobaan. Tunggu beberapa saat.", message)
    }

    @Test
    fun `already registered auth error is recognized without exposing details`() {
        val error = IllegalStateException(
            "User already registered URL: https://project.example/auth/v1/signup",
        )

        assertEquals(true, error.isAlreadyRegisteredAuthError())
    }
}
