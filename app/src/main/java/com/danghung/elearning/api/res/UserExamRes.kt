package com.danghung.elearning.api.res

import java.io.Serializable

data class UserExamRes(
    val id: Long,
    val userId: Long,
    val examId: Long,
    val startedAt: String,
    val parts: List<UserExamPartRes>
): Serializable
