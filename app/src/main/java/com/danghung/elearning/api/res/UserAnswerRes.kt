package com.danghung.elearning.api.res

import java.io.Serializable

data class UserAnswerRes(
    val id: Long?,
    val question: QuestionRes?,
    val selectedOptionId: Long?,
    val answerText: String?,
    val audioUrl: String?,
    val score: Double?,
    val aiScore: Double?,
    val aiFeedback: String?
): Serializable