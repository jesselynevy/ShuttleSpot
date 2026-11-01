package com.beg.shuttlespot.core.common

// Overview: Defines validation for the core common area of ShuttleSpot.

object Validation {
    private const val STUDENT_DOMAIN = "student.umn.ac.id"

    // Handles the is student email responsibility.
    fun isStudentEmail(value: String): Boolean {
        val email = value.trim().lowercase()
        val parts = email.split("@")
        return parts.size == 2 && parts[0].isNotBlank() && parts[1] == STUDENT_DOMAIN
    }

    // Handles the normalize indonesian phone responsibility.
    fun normalizeIndonesianPhone(value: String): String {
        val digits = value.filter(Char::isDigit)
        return when {
            digits.startsWith("62") -> "+$digits"
            digits.startsWith("0") -> "+62${digits.drop(1)}"
            else -> "+$digits"
        }
    }

    // Handles the require ordered stops responsibility.
    fun requireOrderedStops(pickupSequence: Int, destinationSequence: Int) {
        require(pickupSequence < destinationSequence) { "Tujuan harus berada setelah halte jemput." }
    }
}
