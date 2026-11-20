package com.danghung.elearning.api.res

import java.io.Serializable

data class UserExamPartRes(
    val id: Long,
    val userExamId: Long,
    val skillType: String,
    val submitted: Boolean,
    val score: Double?
): Serializable