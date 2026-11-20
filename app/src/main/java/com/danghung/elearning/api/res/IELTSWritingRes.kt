package com.danghung.elearning.api.res

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class IELTSWritingRes(
    val status: String?,

    @SerializedName("overall_band")
    val overallBand: Double?,

    @SerializedName("criteria_scores")
    val criteriaScores: CriteriaScores?,

    @SerializedName("detailed_feedback")
    val detailedFeedback: DetailedFeedback?,

    @SerializedName("examiner_feedback")
    val examinerFeedback: String?,

    val strengths: List<String>?,

    @SerializedName("areas_for_improvement")
    val areasForImprovement: List<String>?,

    val confidence: Double?,

    val evidence: List<Evidence>?,

    val review: Review?,

    @SerializedName("retrieval_document_ids")
    val retrievalDocumentIds: List<String>?,

    @SerializedName("retrieval_index_version")
    val retrievalIndexVersion: String?
): Serializable{
    data class CriteriaScores(
        @SerializedName("task_achievement")
        val taskAchievement: Double?,

        @SerializedName("coherence_cohesion")
        val coherenceCohesion: Double?,

        @SerializedName("lexical_resource")
        val lexicalResource: Double?,

        @SerializedName("grammatical_range_accuracy")
        val grammaticalRange: Double?
    ): Serializable

    data class DetailedFeedback(
        @SerializedName("task_achievement")
        val taskAchievement: String?,

        @SerializedName("coherence_cohesion")
        val coherenceCohesion: String?,

        @SerializedName("lexical_resource")
        val lexicalResource: String?,

        @SerializedName("grammatical_range_accuracy")
        val grammaticalRange: String?
    ): Serializable

    data class Evidence(
        @SerializedName("evidence_id")
        val evidenceId: String?,
        val start: Int?,
        val end: Int?,
        val quote: String?,
        val criterion: String?,
        val signal: String?,
        val reason: String?
    ): Serializable

    data class Review(
        val required: Boolean?,
        val performed: Boolean?,
        val decision: String?,
        @SerializedName("reason_codes")
        val reasonCodes: List<String>?,
        val rationale: String?
    ): Serializable
}


