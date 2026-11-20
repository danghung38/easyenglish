package com.danghung.elearning.api.req

import java.io.Serializable

data class UserExamReq(
    val examId: Long,
    val skillType: String? // "LISTENING", "READING", null = full test
): Serializable