package com.danghung.elearning.model

import com.danghung.elearning.api.res.ExamPartRes
import com.danghung.elearning.api.res.ExamRes

data class StartPracticeData(
    val userExamPartId: Long,
    val exam: ExamRes,
    val examPartRes: ExamPartRes? = null
)