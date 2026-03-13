package com.danghung.elearning.api.req

import java.io.Serializable

data class SubmitRLPartReq(
    val userExamPartId: Long,
    val answers: List<AnswerRLPartReq>
): Serializable
