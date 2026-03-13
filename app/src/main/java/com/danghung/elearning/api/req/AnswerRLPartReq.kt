package com.danghung.elearning.api.req

import java.io.Serializable

data class AnswerRLPartReq(
    val questionId: Long,
    val selectedOptionId: Long
): Serializable