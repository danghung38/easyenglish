package com.danghung.elearning.api.res


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class IELTSSpeakingRes(

    @SerializedName("overall_band")
    val overallBand: Double?,

    @SerializedName("criteria_scores")
    val criteriaScores: SpeakingCriteriaScores?,

    @SerializedName("detailed_feedback")
    val detailedFeedback: String?,

    @SerializedName("examiner_feedback")
    val examinerFeedback: String?,

    val strengths: List<String>?,

    @SerializedName("areas_for_improvement")
    val areasForImprovement: List<String>?,

    val timestamp: String?,

    val status: String? = null,

    val confidence: Double? = null,

    @SerializedName("pronunciation_status")
    val pronunciationStatus: String? = null,

    @SerializedName("audio_evidence")
    val audioEvidence: List<Map<String, Any?>>? = null,

    val review: Map<String, Any?>? = null,
    @SerializedName("score_status") val scoreStatus: String? = null,
    @SerializedName("feedback_status") val feedbackStatus: String? = null,
    val feedback: SpeakingFeedbackDetail? = null,
    @SerializedName("language_band") val languageBand: Double? = null,
    @SerializedName("score_scope") val scoreScope: String? = null,
    @SerializedName("review_error") val reviewError: Map<String, Any?>? = null

) : Serializable{
    data class SpeakingFeedbackDetail(
        @SerializedName("criterion_feedback") val criterionFeedback: Map<String, String>?,
        val strengths: List<String>?,
        @SerializedName("priority_improvements") val priorityImprovements: List<String>?,
        @SerializedName("next_actions") val nextActions: List<String>?
    ) : Serializable
    data class SpeakingCriteriaScores(

        @SerializedName("fluency_coherence")
        val fluencyCoherence: Double?,

        @SerializedName("lexical_resource")
        val lexicalResource: Double?,

        @SerializedName("grammatical_range_accuracy")
        val grammaticalRange: Double?,

        val pronunciation: Double?

    ) : Serializable
}


