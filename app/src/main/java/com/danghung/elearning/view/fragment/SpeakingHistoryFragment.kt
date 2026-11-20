package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.danghung.elearning.api.res.EvaluationJobRes
import com.danghung.elearning.api.res.IELTSSpeakingRes
import com.danghung.elearning.api.res.UserAnswerRes
import com.danghung.elearning.databinding.FragmentSpeakingHistoryBinding
import com.danghung.elearning.evaluation.SpeakingResultFormatter
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.SpeakingVM
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Shows session feedback once, followed by every saved part/transcript. */
@SuppressLint("SetTextI18n")
class SpeakingHistoryFragment : BaseFragment<FragmentSpeakingHistoryBinding, SpeakingVM>() {
    companion object { val TAG: String = SpeakingHistoryFragment::class.java.name }
    private var userExamPartId = 0L
    private val gson = Gson()
    private var poll: Job? = null
    private var pollCount = 0
    private var sessionLoaded = false

    override fun getClassVM(): Class<SpeakingVM> = SpeakingVM::class.java
    override fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSpeakingHistoryBinding.inflate(inflater, container, false)

    override fun initViews() {
        userExamPartId = data as? Long ?: 0L
        callBack.showLoad()
        viewModel.getSpeakingHistory(userExamPartId)
        binding.ivBack.setOnClickListener { callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false) }
        binding.btnRequestFeedback.setOnClickListener {
            binding.btnRequestFeedback.isEnabled = false
            viewModel.requestSpeakingFeedback(userExamPartId)
        }
        setUpOnBackMenu(TYPE_LEARN)
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            SpeakingVM.GET_SPEAKING_HISTORY -> bindAnswers(data as? List<UserAnswerRes> ?: emptyList())
            SpeakingVM.SPEAKING_RESULT, SpeakingVM.SPEAKING_FEEDBACK -> {
                val job = data as? EvaluationJobRes ?: return
                val score = job.result?.let { gson.fromJson(gson.toJson(it), IELTSSpeakingRes::class.java) }
                bindScore(score, true)
                if (job.shouldPollForFinalScore() || score?.feedbackStatus in setOf("QUEUED", "GENERATING")) {
                    if (++pollCount <= 180) {
                        poll?.cancel()
                        poll = viewLifecycleOwner.lifecycleScope.launch {
                            delay(2000)
                            viewModel.getSpeakingResult(userExamPartId)
                        }
                    } else {
                        binding.tvStatus.text = "Xử lý lâu hơn dự kiến. Mở lại Detail để kiểm tra."
                        binding.tvStatus.isVisible = true
                    }
                }
            }
        }
    }

    private fun bindAnswers(answers: List<UserAnswerRes>) {
        val sorted = answers.sortedWith(compareBy({ it.question?.section.orEmpty() }, { it.question?.id ?: 0L }))
        binding.layoutSpeakingAnswers.removeAllViews()
        var sessionScore: IELTSSpeakingRes? = null
        var hasJob = false
        for (answer in sorted) {
            val metadata = runCatching { gson.fromJson(answer.aiFeedback, JsonObject::class.java) }.getOrNull()
            val score = runCatching { gson.fromJson(answer.aiFeedback, IELTSSpeakingRes::class.java) }.getOrNull()
            if (sessionScore == null || metadata?.get("scope")?.asString == "full_session") sessionScore = score
            hasJob = hasJob || metadata?.has("job_id") == true
            val prompts = metadata?.get("question_text")?.asString ?: answer.question?.content.orEmpty()
            val title = answer.question?.section?.replace("part", "Part ") ?: "Answer"
            val view = TextView(requireContext()).apply {
                text = Html.fromHtml("<b>$title</b><br>" + prompts.replace("\n", "<br><br>"), Html.FROM_HTML_MODE_COMPACT)
                append("\n\nTranscript\n" + answer.answerText.orEmpty() + "\n")
                textSize = 16f
                setTextColor(android.graphics.Color.BLACK)
                val padding = (16 * resources.displayMetrics.density).toInt()
                setPadding(padding, padding, padding, padding)
                setBackgroundColor(android.graphics.Color.WHITE)
                setTextIsSelectable(true)
                layoutParams = android.widget.LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = padding }
            }
            binding.layoutSpeakingAnswers.addView(view)
        }
        if (answers.isEmpty()) {
            binding.tvStatus.text = "Chưa có bài làm Speaking."
            binding.tvStatus.isVisible = true
        }
        else bindScore(sessionScore, hasJob)
        if (hasJob && !sessionLoaded) {
            sessionLoaded = true
            viewModel.getSpeakingResult(userExamPartId)
        }
    }

    private fun bindScore(score: IELTSSpeakingRes?, hasJob: Boolean) {
        binding.tvAiScore.text = if (score?.overallBand != null) "AI-estimated band: " + SpeakingResultFormatter.band(score.overallBand)
            else "Language estimate (3 criteria): " + SpeakingResultFormatter.band(score?.languageBand)
        binding.tvCriteria.text = SpeakingResultFormatter.criteria(score)
        binding.tvStatus.text = SpeakingResultFormatter.notice(score)
        binding.tvStatus.isVisible = binding.tvStatus.text.isNotBlank()
        val feedback = score?.feedback
        binding.tvFeedback.text = listOf(
            "Điểm mạnh" to (feedback?.strengths ?: score?.strengths),
            "Ưu tiên cải thiện" to (feedback?.priorityImprovements ?: score?.areasForImprovement),
            "Bài tập tiếp theo" to feedback?.nextActions
        ).filter { !it.second.isNullOrEmpty() }.joinToString("\n\n") { (label, values) -> label + "\n" + values?.joinToString("\n") { "• $it" } }
        binding.btnRequestFeedback.isVisible = hasJob && score?.feedbackStatus != "COMPLETED"
        binding.btnRequestFeedback.isEnabled = score?.feedbackStatus !in setOf("QUEUED", "GENERATING")
    }

    override fun onDestroyView() {
        poll?.cancel()
        super.onDestroyView()
    }
}
