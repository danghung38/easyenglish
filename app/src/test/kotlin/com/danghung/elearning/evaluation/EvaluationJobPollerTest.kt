package com.danghung.elearning.evaluation

import com.danghung.elearning.api.res.EvaluationJobRes
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluationJobPollerTest {
    @Test
    fun pollsUntilCompletedAndReportsProgress() = runBlocking {
        val statuses = ArrayDeque(listOf("PENDING", "SCORING", "COMPLETED"))
        val observed = mutableListOf<String>()
        val result = EvaluationJobPoller(intervalMillis = 0, maxAttempts = 5).poll(
            fetch = {
                EvaluationJobRes(
                    jobId = "job-1",
                    skill = "writing",
                    status = statuses.removeFirst()
                )
            },
            onProgress = { observed += it.status }
        )

        assertEquals("COMPLETED", result.status)
        assertEquals(listOf("PENDING", "SCORING", "COMPLETED"), observed)
    }

    @Test
    fun rejectedAndFailedAreTerminalInsteadOfFakeScores() = runBlocking {
        for (status in listOf("REJECTED", "FAILED")) {
            val result = EvaluationJobPoller(intervalMillis = 0, maxAttempts = 2).poll(
                fetch = {
                    EvaluationJobRes(jobId = "job", skill = "speaking", status = status)
                }
            )
            assertTrue(result.isTerminal())
            assertEquals(null, result.result)
        }
    }

    @Test
    fun completedJobCanExposeProvisionalScoreWhileReviewRuns() {
        val job = EvaluationJobRes(
            jobId = "job",
            skill = "writing",
            status = "COMPLETED",
            result = mapOf(
                "score_status" to "PROVISIONAL",
                "feedback_status" to "NOT_REQUESTED"
            )
        )
        assertTrue(job.hasDisplayableScore())
        assertTrue(job.shouldPollForFinalScore())
        assertEquals("PROVISIONAL", job.scoreStatus())
        assertEquals("NOT_REQUESTED", job.feedbackStatus())
    }

    @Test
    fun stopsPollingOnlyAfterReviewerPublishesFinalScore() {
        val finalJob = EvaluationJobRes(
            jobId = "job",
            skill = "speaking",
            status = "COMPLETED",
            result = mapOf("score_status" to "FINAL")
        )
        val reviewFailedJob = EvaluationJobRes(
            jobId = "job",
            skill = "speaking",
            status = "COMPLETED",
            result = mapOf(
                "score_status" to "PROVISIONAL",
                "review_error" to "Reviewer timeout"
            )
        )

        assertEquals(false, finalJob.shouldPollForFinalScore())
        assertEquals(false, reviewFailedJob.shouldPollForFinalScore())
    }
}
