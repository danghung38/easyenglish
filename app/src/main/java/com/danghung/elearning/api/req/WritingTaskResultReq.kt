package com.danghung.elearning.api.req

import java.io.Serializable

data class WritingTaskResultReq(
    val questionId: Long,
    val essayText: String,
    val wordCount: Int,
    val duration: Int,
    val overallBand: Double? = null,
    val taskAchievement: Double? = null,
    val coherenceCohesion: Double? = null,
    val lexicalResource: Double? = null,
    val grammaticalRange: Double? = null,
    val detailedFeedback: String? = null,
    val examinerFeedback: String? = null
): Serializable
