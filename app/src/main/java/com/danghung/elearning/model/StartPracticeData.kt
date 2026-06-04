package com.danghung.elearning.model

import com.danghung.elearning.api.res.ExamPartRes
import com.danghung.elearning.api.res.ExamRes
import com.danghung.elearning.api.res.UserExamRes

data class StartPracticeData(
    val userExamPartId: Long,
    val exam: ExamRes,
    val examPartRes: ExamPartRes? = null,
    val userExamRes: UserExamRes? = null
)
