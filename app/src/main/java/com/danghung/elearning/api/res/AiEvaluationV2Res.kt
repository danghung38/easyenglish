package com.danghung.elearning.api.res

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class AiCriterionV2Res(
    val band: Double?,
    val confidence: Double?,
    val evidenceIds: List<String>?,
    val feedback: String?
)

data class AiReviewV2Res(
    val required: Boolean?,
    val performed: Boolean?,
    val reasonCodes: List<String>?
)

data class AiVersionsV2Res(
    val model: String?,
    val rubric: String?,
    val prompts: String?,
    val retrievalIndex: String?
)

data class AiEvaluationV2Res(
    @SerializedName("schemaVersion") val schemaVersion: String?,
    val status: String?,
    val requestId: String?,
    val idempotencyKey: String?,
    val skill: String?,
    val task: String?,
    val criteria: Map<String, AiCriterionV2Res>?,
    val taskBand: Double?,
    val overallBand: Double?,
    val confidence: Double?,
    val review: AiReviewV2Res?,
    val strengths: List<String>?,
    val priorityImprovements: List<String>?,
    val evidence: List<Map<String, Any>>?,
    val versions: AiVersionsV2Res?
)

object AiEvaluationV2Contract {
    const val SCHEMA_VERSION = "2.0"

    fun parse(json: String, gson: Gson = Gson()): AiEvaluationV2Res {
        val result = gson.fromJson(json, AiEvaluationV2Res::class.java)
            ?: throw IllegalArgumentException("AI evaluation response is null")
        validate(result)
        return result
    }

    fun validate(result: AiEvaluationV2Res) {
        require(result.schemaVersion == SCHEMA_VERSION) {
            "Unsupported AI schema version: ${result.schemaVersion}"
        }
        requireNotNull(result.status) { "Missing required field: status" }
        requireNotNull(result.requestId) { "Missing required field: requestId" }
        requireNotNull(result.idempotencyKey) { "Missing required field: idempotencyKey" }
        requireNotNull(result.skill) { "Missing required field: skill" }
        val criteria = requireNotNull(result.criteria) {
            "Missing required field: criteria"
        }
        require(criteria.isNotEmpty()) { "criteria must not be empty" }
        criteria.forEach { (name, criterion) ->
            requireNotNull(criterion.band) { "Missing criteria.$name.band" }
            requireNotNull(criterion.evidenceIds) { "Missing criteria.$name.evidenceIds" }
            requireNotNull(criterion.feedback) { "Missing criteria.$name.feedback" }
        }
        requireNotNull(result.review) { "Missing required field: review" }
        requireNotNull(result.strengths) { "Missing required field: strengths" }
        requireNotNull(result.priorityImprovements) {
            "Missing required field: priorityImprovements"
        }
        requireNotNull(result.evidence) { "Missing required field: evidence" }
        requireNotNull(result.versions) { "Missing required field: versions" }
    }
}
