package com.danghung.elearning.view.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.danghung.elearning.api.res.UserAnswerRes
import com.danghung.elearning.databinding.ItemWritingHistoryBinding
import com.google.gson.Gson
import java.util.Locale

class WritingHistoryAdapter(
    private val gson: Gson = Gson()
) : RecyclerView.Adapter<WritingHistoryAdapter.WritingHistoryViewHolder>() {

    private var items: List<UserAnswerRes> = emptyList()

    fun submitList(values: List<UserAnswerRes>) {
        items = WritingHistoryPresentation.sortAnswers(values)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WritingHistoryViewHolder {
        val binding = ItemWritingHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WritingHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WritingHistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class WritingHistoryViewHolder(
        private val binding: ItemWritingHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserAnswerRes) = with(binding) {
            val feedback = parseFeedback(item.aiFeedback)
            val score = item.aiScore ?: WritingHistoryPresentation.computeScore(feedback)
            tvTaskLabel.text = WritingHistoryPresentation.taskLabel(item.question?.section)
            tvAiScore.text = WritingHistoryPresentation.bandLabel(score)
            tvQuestion.text = Html.fromHtml(
                item.question?.content.orEmpty(),
                Html.FROM_HTML_MODE_COMPACT
            )
            tvAnswer.text = item.answerText.orDash()
            tvCriteria.text = WritingHistoryPresentation.criteriaText(feedback)
            tvDetailedFeedback.text = WritingHistoryPresentation.detailedText(feedback)
            tvExaminerFeedback.text = WritingHistoryPresentation.summaryText(feedback).orDash()
        }
    }

    private fun parseFeedback(value: String?): WritingFeedback? {
        if (value.isNullOrBlank()) return null
        return runCatching { gson.fromJson(value, WritingFeedback::class.java) }.getOrNull()
    }

    data class WritingFeedback(
        val taskAchievement: Double?,
        val coherenceCohesion: Double?,
        val lexicalResource: Double?,
        val grammaticalRange: Double?,
        val detailedFeedback: DetailedFeedback?,
        val examinerFeedback: String?,
        val confidence: Double?,
        val strengths: List<String>?,
        val areasForImprovement: List<String>?,
        val evidence: List<Evidence>?,
        val review: Review?
    )

    data class DetailedFeedback(
        val taskAchievement: String?,
        val coherenceCohesion: String?,
        val lexicalResource: String?,
        val grammaticalRange: String?
    )

    data class Evidence(
        val evidenceId: String?,
        val quote: String?,
        val criterion: String?,
        val signal: String?,
        val reason: String?
    )

    data class Review(
        val required: Boolean?,
        val performed: Boolean?,
        val decision: String?,
        val reasonCodes: List<String>?,
        val rationale: String?
    )

    private fun String?.orDash(): String = this?.ifBlank { "-" } ?: "-"
}

object WritingHistoryPresentation {
    fun sortAnswers(values: List<UserAnswerRes>): List<UserAnswerRes> =
        values.sortedWith(
            compareBy<UserAnswerRes> { taskOrder(it.question?.section) }
                .thenBy { it.question?.id }
        )

    fun taskOrder(section: String?): Int = when (section?.lowercase(Locale.US)) {
        "task1" -> 1
        "task2" -> 2
        else -> 3
    }

    fun taskLabel(section: String?): String = when (section?.lowercase(Locale.US)) {
        "task1" -> "TASK 1"
        "task2" -> "TASK 2"
        else -> "WRITING"
    }

    fun bandLabel(score: Double?): String = score?.let {
        String.format(Locale.US, "AI Band %.1f", it)
    } ?: "AI Band -"

    fun computeScore(feedback: WritingHistoryAdapter.WritingFeedback?): Double? {
        if (feedback == null) return null
        val values = listOfNotNull(
            feedback.taskAchievement,
            feedback.coherenceCohesion,
            feedback.lexicalResource,
            feedback.grammaticalRange
        )
        if (values.isEmpty()) return null
        return kotlin.math.round(values.average() * 2.0) / 2.0
    }

    fun criteriaText(feedback: WritingHistoryAdapter.WritingFeedback?): String =
        "Task: ${feedback?.taskAchievement ?: "-"} | " +
            "Coherence: ${feedback?.coherenceCohesion ?: "-"} | " +
            "Lexical: ${feedback?.lexicalResource ?: "-"} | " +
            "Grammar: ${feedback?.grammaticalRange ?: "-"}"

    fun detailedText(feedback: WritingHistoryAdapter.WritingFeedback?): String {
        val details = feedback?.detailedFeedback
        val evidence = feedback?.evidence
            ?.take(5)
            ?.joinToString("\n") { item ->
                "• [${item.criterion ?: "criterion"}] “${item.quote.orEmpty()}” — " +
                    (item.reason ?: "-")
            }
            ?.ifBlank { "-" }
            ?: "-"
        val confidence = feedback?.confidence?.let {
            String.format(Locale.US, "%.0f%%", it * 100)
        } ?: "-"
        return "Task: ${details?.taskAchievement ?: "-"}\n" +
            "Coherence: ${details?.coherenceCohesion ?: "-"}\n" +
            "Lexical: ${details?.lexicalResource ?: "-"}\n" +
            "Grammar: ${details?.grammaticalRange ?: "-"}\n\n" +
            "Confidence: $confidence\nEvidence:\n$evidence"
    }

    fun summaryText(feedback: WritingHistoryAdapter.WritingFeedback?): String {
        val review = feedback?.review?.let {
            "Review: ${it.decision ?: "-"}" +
                if (it.reasonCodes.isNullOrEmpty()) "" else " (${it.reasonCodes.joinToString()})"
        }
        return listOfNotNull(
            feedback?.examinerFeedback,
            feedback?.strengths?.takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "Strengths: ", separator = "; "),
            feedback?.areasForImprovement?.takeIf { it.isNotEmpty() }
                ?.joinToString(prefix = "Priorities: ", separator = "; "),
            review
        ).joinToString("\n")
    }
}
