package com.danghung.elearning.api.req

data class SubmitWritingExamReq(
    val userExamPartId: Long,
    val overallBand: Double,
    val taskResults: List<WritingTaskResultReq>
)

