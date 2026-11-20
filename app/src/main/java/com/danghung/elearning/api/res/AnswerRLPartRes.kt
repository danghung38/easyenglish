package com.danghung.elearning.api.res

import java.io.Serializable

data class AnswerRLPartRes(
    val id: Long,
    val selectedOptionId: Long,
    val score: Double,
    val questionRes: QuestionRes
): Serializable