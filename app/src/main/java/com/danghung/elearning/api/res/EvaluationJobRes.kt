package com.danghung.elearning.api.res

import java.io.Serializable

data class EvaluationJobRes(
    val jobId: String,
    val requestId: String? = null,
    val idempotencyKey: String? = null,
    val skill: String,
    val status: String,
    val attemptCount: Int = 0,
    val progress: Int = 0,
    val result: Map<String, Any?>? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Serializable {
    fun isTerminal(): Boolean = status in TERMINAL_STATUSES

    fun scoreStatus(): String? = result?.get("score_status") as? String

    fun feedbackStatus(): String? = result?.get("feedback_status") as? String

    fun shouldPollForFinalScore(): Boolean =
        !isTerminal() || (
            hasDisplayableScore() &&
                scoreStatus() == "PROVISIONAL" &&
                result?.get("review_error") == null
            )

    fun hasDisplayableScore(): Boolean =
        status == "COMPLETED" && result?.get("status") != "REJECTED" && scoreStatus() in setOf("PROVISIONAL", "FINAL")

    companion object {
        val TERMINAL_STATUSES = setOf("COMPLETED", "REJECTED", "FAILED", "CANCELLED")
    }
}
