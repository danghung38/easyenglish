package com.danghung.elearning.api.res

import java.io.Serializable

data class WritingResultRes(
    val overallBand: Double,
    val taskAchievement: Double,
    val coherenceCohesion: Double,
    val lexicalResource: Double,
    val grammaticalRange: Double,
    val detailedFeedback: String?,
    val examinerFeedback: String?
): Serializable
