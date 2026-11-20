package com.danghung.elearning.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class UserInputValidatorTest {
    private val today = LocalDate.of(2026, 10, 6)

    @Test
    fun validAdultDateReturnsNoError() {
        assertNull(UserInputValidator.dateOfBirthError("1998/06/15", today))
    }

    @Test
    fun exactlyEighteenYearsOldIsAccepted() {
        assertNull(UserInputValidator.dateOfBirthError("2008/10/06", today))
    }

    @Test
    fun malformedOrImpossibleDateIsRejected() {
        assertEquals(
            UserInputValidator.INVALID_DATE_MESSAGE,
            UserInputValidator.dateOfBirthError("2000/02/30", today)
        )
        assertEquals(
            UserInputValidator.INVALID_DATE_MESSAGE,
            UserInputValidator.dateOfBirthError("06/10/2000", today)
        )
    }

    @Test
    fun userUnderEighteenIsRejected() {
        assertEquals(
            UserInputValidator.MINIMUM_AGE_MESSAGE,
            UserInputValidator.dateOfBirthError("2008/10/07", today)
        )
    }
}
