package com.danghung.elearning.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object UserInputValidator {
    const val DATE_FORMAT = "yyyy/MM/dd"
    const val INVALID_DATE_MESSAGE = "Date of Birth is invalid. Please use format $DATE_FORMAT"
    const val MINIMUM_AGE_MESSAGE = "You must be at least 18 years old"

    private val dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd")
        .withResolverStyle(ResolverStyle.STRICT)

    fun dateOfBirthError(
        value: String,
        today: LocalDate = LocalDate.now()
    ): String? {
        val dateOfBirth = parseDate(value) ?: return INVALID_DATE_MESSAGE
        return if (dateOfBirth.plusYears(18).isAfter(today)) MINIMUM_AGE_MESSAGE else null
    }

    fun isValidDate(value: String): Boolean = parseDate(value) != null

    private fun parseDate(value: String): LocalDate? = runCatching {
        LocalDate.parse(value, dateFormatter)
    }.getOrNull()
}
