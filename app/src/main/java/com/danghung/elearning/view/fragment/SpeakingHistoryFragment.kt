package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.danghung.elearning.api.res.UserAnswerRes
import com.danghung.elearning.databinding.FragmentSpeakingHistoryBinding
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.SpeakingVM
import com.danghung.elearning.viewmodel.SpeakingVM.Companion.GET_SPEAKING_HISTORY
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.Locale

@SuppressLint("SetTextI18n")
class SpeakingHistoryFragment : BaseFragment<FragmentSpeakingHistoryBinding, SpeakingVM>() {
    companion object {
        val TAG: String = SpeakingHistoryFragment::class.java.name
    }

    private var userExamPartId: Long = 0L
    private val gson = Gson()

    override fun getClassVM(): Class<SpeakingVM> = SpeakingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSpeakingHistoryBinding =
        FragmentSpeakingHistoryBinding.inflate(inflater, container, false)

    override fun initViews() {
        setUpNoOnBackPressed()
        userExamPartId = data as? Long ?: 0L

        callBack.showLoading()
        viewModel.getSpeakingHistory(userExamPartId)

        binding.ivBack.setOnClickListener {
            callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_SPEAKING_HISTORY -> {
                val res = data as? List<UserAnswerRes> ?: emptyList()
                bindResult(res.firstOrNull())
            }
        }
    }

    private fun bindResult(item: UserAnswerRes?) {
        val root = binding.layoutSpeakingResult
        val questionView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvQuestion)
        val transcriptView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvTranscript)
        val aiScoreView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvAiScore)
        val criteriaView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvCriteria)
        val examinerView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvExaminerFeedback)
        val strengthsView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvStrengths)
        val improvementsView = root.findViewById<TextView>(com.danghung.elearning.R.id.tvImprovements)

        fun setOrDash(view: TextView, value: String?) {
            view.text = value?.ifBlank { "-" } ?: "-"
        }

        if (item == null) {
            questionView.text = "No result"
            setOrDash(transcriptView, null)
            aiScoreView.text = "AI Score: 0.0"
            setOrDash(criteriaView, null)
            setOrDash(examinerView, null)
            setOrDash(strengthsView, null)
            setOrDash(improvementsView, null)
            return
        }

        questionView.text = Html.fromHtml(item.question?.content.orEmpty(), Html.FROM_HTML_MODE_COMPACT)
        setOrDash(transcriptView, item.answerText)

        val feedback = parseFeedback(item.aiFeedback)
        val aiScore = item.aiScore ?: feedback?.overallBand ?: 0.0
        aiScoreView.text = String.format(Locale.US, "AI Score: %.1f", aiScore)

        val criteria = feedback?.criteriaScores
        val criteriaText = "Pronunciation: ${criteria?.pronunciation ?: "-"} | " +
            "Fluency: ${criteria?.fluencyCoherence ?: "-"} | " +
            "Lexical: ${criteria?.lexicalResource ?: "-"} | " +
            "Grammar: ${criteria?.grammaticalRangeAccuracy ?: "-"}"
        criteriaView.text = criteriaText

        setOrDash(examinerView, feedback?.examinerFeedback)
        setOrDash(strengthsView, feedback?.strengths?.joinToString("; "))
        setOrDash(improvementsView, feedback?.areasForImprovement?.joinToString("; "))
    }

    private fun parseFeedback(aiFeedback: String?): SpeakingFeedback? {
        if (aiFeedback.isNullOrBlank()) return null
        return try {
            gson.fromJson(aiFeedback, SpeakingFeedback::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    data class SpeakingFeedback(
        @SerializedName("criteria_scores")
        val criteriaScores: CriteriaScores?,
        @SerializedName("examiner_feedback")
        val examinerFeedback: String?,
        @SerializedName("overall_band")
        val overallBand: Double?,
        val strengths: List<String>?,
        @SerializedName("areas_for_improvement")
        val areasForImprovement: List<String>?
    )

    data class CriteriaScores(
        val pronunciation: Double?,
        @SerializedName("fluency_coherence")
        val fluencyCoherence: Double?,
        @SerializedName("lexical_resource")
        val lexicalResource: Double?,
        @SerializedName("grammatical_range_accuracy")
        val grammaticalRangeAccuracy: Double?
    )
}
