package com.beg.shuttlespot.data.remote

internal const val PASSWORD_COMPLEXITY_MESSAGE =
    "Password should contain character and digits."

internal fun Throwable.isAlreadyRegisteredAuthError(): Boolean =
    "user already registered" in message.orEmpty().lowercase()

internal fun Throwable.toSafeAuthMessage(): String {
    val rawMessage = message.orEmpty().lowercase()

    return when {
        "weak_password" in rawMessage ||
            "password should contain" in rawMessage ->
            PASSWORD_COMPLEXITY_MESSAGE

        "email not confirmed" in rawMessage ->
            "Email belum dikonfirmasi. Periksa inbox atau kirim ulang email konfirmasi."

        "invalid login credentials" in rawMessage ->
            "Email atau password salah."

        "user already registered" in rawMessage ->
            "Email tersebut sudah terdaftar."

        "rate limit" in rawMessage ||
            "rate_limit" in rawMessage ||
            "over_email_send_rate_limit" in rawMessage ->
            "Terlalu banyak percobaan. Tunggu beberapa saat."

        else ->
            "Operasi autentikasi gagal. Silakan coba lagi."
    }
}
