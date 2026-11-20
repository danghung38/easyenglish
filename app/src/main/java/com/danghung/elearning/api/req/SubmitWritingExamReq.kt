package com.danghung.elearning.api.req

data class SubmitWritingExamReq(
    val userExamPartId: Long,
    // Deprecated compatibility field. Backend recomputes the official value.
    val overallBand: Double? = null,
    val taskResults: List<WritingTaskResultReq>
)

