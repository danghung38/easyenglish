package com.danghung.elearning.view.fragment

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.danghung.elearning.api.res.UserAnswerRes
import com.danghung.elearning.databinding.FragmentWritingHistoryBinding
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.WritingVM
import com.danghung.elearning.viewmodel.WritingVM.Companion.GET_WRITING_HISTORY
import com.google.gson.Gson
import java.util.Locale

class WritingHistoryFragment : BaseFragment<FragmentWritingHistoryBinding, WritingVM>() {
    companion object {
        val TAG: String = WritingHistoryFragment::class.java.name
    }

    private var userExamPartId: Long = 0L
    private val gson = Gson()

    override fun getClassVM(): Class<WritingVM> = WritingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentWritingHistoryBinding =
        FragmentWritingHistoryBinding.inflate(inflater, container, false)

    override fun initViews() {
        setUpNoOnBackPressed()
        userExamPartId = data as? Long ?: 0L
        callBack.showLoad()
        //callBack.showLoading()
        viewModel.getWritingHistory(userExamPartId)

        binding.ivBack.setOnClickListener {
            callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_WRITING_HISTORY -> {
                val res = data as? List<UserAnswerRes> ?: emptyList()
                bindResult(res.firstOrNull())
            }
        }
    }

    private fun bindResult(item: UserAnswerRes?) {
        val root = binding.layoutWritingResult
        val questionView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvQuestion)
        val answerView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvAnswer)
        val aiScoreView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvAiScore)
        val criteriaView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvCriteria)
        val detailedView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvDetailedFeedback)
        val examinerView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvExaminerFeedback)

        fun setOrDash(view: TextView, value: String?) {
            view.text = value?.ifBlank { "-" } ?: "-"
        }

        if (item == null) {
            questionView.text = "No result"
            setOrDash(answerView, null)
            aiScoreView.text = "AI Score: 0.0"
            setOrDash(criteriaView, null)
            setOrDash(detailedView, null)
            setOrDash(examinerView, null)
            return
        }

        questionView.text = Html.fromHtml(item.question?.content.orEmpty(), Html.FROM_HTML_MODE_COMPACT)
        setOrDash(answerView, item.answerText)

        val feedback = parseFeedback(item.aiFeedback)
        val aiScore = item.aiScore ?: computeAiScore(feedback)
        aiScoreView.text = if (aiScore == null) {
            "AI Score: -"
        } else {
            String.format(Locale.US, "AI Score: %.1f", aiScore)
        }

        criteriaView.text = "Task: ${feedback?.taskAchievement ?: "-"} | " +
            "Coherence: ${feedback?.coherenceCohesion ?: "-"} | " +
            "Lexical: ${feedback?.lexicalResource ?: "-"} | " +
            "Grammar: ${feedback?.grammaticalRange ?: "-"}"

        val detailed = feedback?.detailedFeedback
        val detailedText = "Task: ${detailed?.taskAchievement ?: "-"}\n" +
            "Coherence: ${detailed?.coherenceCohesion ?: "-"}\n" +
            "Lexical: ${detailed?.lexicalResource ?: "-"}\n" +
            "Grammar: ${detailed?.grammaticalRange ?: "-"}"
        detailedView.text = detailedText

        setOrDash(examinerView, feedback?.examinerFeedback)
    }

    private fun parseFeedback(aiFeedback: String?): WritingFeedback? {
        if (aiFeedback.isNullOrBlank()) return null
        return try {
            gson.fromJson(aiFeedback, WritingFeedback::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    private fun computeAiScore(feedback: WritingFeedback?): Double? {
        if (feedback == null) return null
        val scores = listOf(
            feedback.taskAchievement,
            feedback.coherenceCohesion,
            feedback.lexicalResource,
            feedback.grammaticalRange
        ).filterNotNull()
        if (scores.isEmpty()) return null
        val avg = scores.average()
        return kotlin.math.round(avg * 2.0) / 2.0
    }

    data class WritingFeedback(
        val taskAchievement: Double?,
        val coherenceCohesion: Double?,
        val lexicalResource: Double?,
        val grammaticalRange: Double?,
        val detailedFeedback: DetailedFeedback?,
        val examinerFeedback: String?
    )

    data class DetailedFeedback(
        val taskAchievement: String?,
        val coherenceCohesion: String?,
        val lexicalResource: String?,
        val grammaticalRange: String?
    )
}
