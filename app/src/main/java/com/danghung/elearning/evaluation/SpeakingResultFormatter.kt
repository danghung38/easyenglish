package com.danghung.elearning.evaluation

import com.danghung.elearning.api.res.IELTSSpeakingRes
import java.util.Locale

object SpeakingResultFormatter {
    private val labels = linkedMapOf(
        "fluency_coherence" to "Fluency & Coherence",
        "lexical_resource" to "Lexical Resource",
        "grammatical_range_accuracy" to "Grammatical Range & Accuracy",
        "pronunciation" to "Pronunciation"
    )
    fun band(value: Double?): String = value?.let { String.format(Locale.US, "%.1f", it) } ?: "--"

    fun criteria(result: IELTSSpeakingRes?): String {
        val scores = result?.criteriaScores
        val bands = listOf(scores?.fluencyCoherence, scores?.lexicalResource, scores?.grammaticalRange, scores?.pronunciation)
        return labels.entries.mapIndexed { index, (key, label) ->
            val comment = result?.feedback?.criterionFeedback?.get(key).orEmpty()
            "$label: ${band(bands[index])}" + if (comment.isBlank()) "" else "\n$comment"
        }.joinToString("\n\n")
    }

    fun notice(result: IELTSSpeakingRes?): String {
        val scoreStatus = when {
            result?.reviewError != null -> "Reviewer gặp lỗi kết nối; giữ điểm tạm tính, chưa xác nhận cuối cùng."
            result?.scoreStatus == "PROVISIONAL" -> "Điểm tạm tính; Reviewer đang kiểm tra."
            else -> null
        }
        val feedbackStatus = when (result?.feedbackStatus) {
            "QUEUED", "GENERATING" -> "Đang tạo nhận xét..."
            "FAILED" -> "Chưa tạo được nhận xét; có thể thử lại."
            else -> null
        }
        return listOfNotNull(scoreStatus, feedbackStatus).joinToString("\n")
    }
}
