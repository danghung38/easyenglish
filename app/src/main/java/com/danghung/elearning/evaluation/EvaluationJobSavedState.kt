package com.danghung.elearning.evaluation

import androidx.lifecycle.SavedStateHandle

class EvaluationJobSavedState(private val state: SavedStateHandle) {
    var jobId: String?
        get() = state[JOB_ID]
        set(value) {
            state[JOB_ID] = value
        }

    fun clear() {
        state[JOB_ID] = null
    }

    companion object {
        private const val JOB_ID = "active_evaluation_job_id"
    }
}
