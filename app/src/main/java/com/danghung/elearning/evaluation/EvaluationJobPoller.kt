package com.danghung.elearning.evaluation

import com.danghung.elearning.api.res.EvaluationJobRes
import kotlinx.coroutines.delay

class EvaluationJobPoller(
    private val intervalMillis: Long = 1_000,
    private val maxAttempts: Int = 300
) {
    suspend fun poll(
        fetch: suspend () -> EvaluationJobRes,
        onProgress: (EvaluationJobRes) -> Unit = {}
    ): EvaluationJobRes {
        repeat(maxAttempts) { attempt ->
            val job = fetch()
            onProgress(job)
            if (job.isTerminal()) return job
            if (attempt < maxAttempts - 1) delay(intervalMillis)
        }
        throw EvaluationPollingTimeout("Evaluation did not finish after $maxAttempts polls")
    }
}

class EvaluationPollingTimeout(message: String) : RuntimeException(message)
