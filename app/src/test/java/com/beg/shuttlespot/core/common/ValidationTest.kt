package com.beg.shuttlespot.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test fun student_email_requires_the_exact_umn_domain() {
        assertTrue(Validation.isStudentEmail("name@student.umn.ac.id"))
        assertFalse(Validation.isStudentEmail("name@sub.student.umn.ac.id"))
        assertFalse(Validation.isStudentEmail("name@student.umn.ac.id.example"))
    }

    @Test fun phone_normalization_is_stable_for_indonesian_forms() {
        assertEquals("+6281234567890", Validation.normalizeIndonesianPhone("081234567890"))
        assertEquals("+6281234567890", Validation.normalizeIndonesianPhone("+62 812-3456-7890"))
    }
}
