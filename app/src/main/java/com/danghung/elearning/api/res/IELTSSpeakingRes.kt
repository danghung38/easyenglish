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

    val timestamp: String?

) : Serializable{
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


