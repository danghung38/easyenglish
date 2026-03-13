package com.danghung.elearning.api.res

import java.io.Serializable

data class TestHistoryRes(
    val userExamId: Long,
    val examId: Long,
    val userExamPartId: Long?,
    val examName: String,
    val testDate: String,
    val isFullTest: Boolean,
    val score: Double,
    val skillType: String?,
    val partScores: List<PartScore>?
): Serializable {

    data class PartScore(
        val skillType: String,
        val score: Double
    ): Serializable
}
