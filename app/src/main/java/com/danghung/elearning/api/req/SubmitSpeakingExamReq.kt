package com.danghung.elearning.api.req

import java.io.Serializable


data class SubmitSpeakingExamReq(
    val userExamPartId: Long,
    @Deprecated("Backend recomputes Speaking overall from criteria")
    val overallBand: Double? = null,
    val fluencyCoherence: Double?,
    val lexicalResource: Double?,
    val grammaticalRange: Double?,
    val pronunciation: Double?,
    val overallFeedback: String?
): Serializable
