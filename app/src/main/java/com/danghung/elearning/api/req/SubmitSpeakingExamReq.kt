package com.danghung.elearning.api.req

import java.io.Serializable


data class SubmitSpeakingExamReq(
    val userExamPartId: Long,
    val overallBand: Double,
    val fluencyCoherence: Double?,
    val lexicalResource: Double?,
    val grammaticalRange: Double?,
    val pronunciation: Double?,
    val overallFeedback: String?
): Serializable