package com.danghung.elearning.api.res

import java.io.Serializable

data class QuestionRes(
    val id: Long,
    val examPartId: Long,
    val skillType: String,
    val isSection: Boolean,
    val content: String?,
    val audioUrl: String?,
    val imageUrl: String?,
    val maxScore: Double?,
    val correctOptionId: Long?,
    val optionRes: List<OptionRes>?,
    val section: String?,
    val explain: String?,
    val transcript: String?
): Serializable{
    data class OptionRes(
        val id: Long,
        val content: String
    ): Serializable
}

