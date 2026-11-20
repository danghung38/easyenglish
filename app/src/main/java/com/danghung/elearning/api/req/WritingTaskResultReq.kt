package com.danghung.elearning.api.req

import java.io.Serializable
import com.danghung.elearning.api.res.IELTSWritingRes

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
    val examinerFeedback: String? = null,
    val confidence: Double? = null,
    val strengths: List<String>? = null,
    val areasForImprovement: List<String>? = null,
    val evidence: List<IELTSWritingRes.Evidence>? = null,
    val review: IELTSWritingRes.Review? = null,
    val retrievalDocumentIds: List<String>? = null,
    val retrievalIndexVersion: String? = null
): Serializable
