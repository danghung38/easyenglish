package com.danghung.elearning.api.res

import java.io.Serializable

data class ExamPartRes(
    val id: Long,
    val examId: Long,
    val skillType: String, // READING, LISTENING...
    val duration: Int
): Serializable