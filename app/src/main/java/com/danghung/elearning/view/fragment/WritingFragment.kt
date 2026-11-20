package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.danghung.elearning.R
import com.danghung.elearning.api.req.SubmitWritingExamReq
import com.danghung.elearning.api.req.WritingTaskResultReq
import com.danghung.elearning.api.res.ApiErrorRes
import com.danghung.elearning.api.res.IELTSWritingRes
import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.api.res.WritingResultRes
import com.danghung.elearning.databinding.FragmentWritingBinding
import com.danghung.elearning.model.StartPracticeData
import com.danghung.elearning.view.dialog.FullTestNextDialog
import com.danghung.elearning.view.dialog.ErrorDialog
import com.danghung.elearning.view.dialog.ExitDialog
import com.danghung.elearning.view.dialog.SubmitSuccessDialog
import com.danghung.elearning.view.dialog.ScoreResultDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_EXAMS
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.WritingVM
import com.danghung.elearning.viewmodel.WritingVM.Companion.EVALUATE_WRITING
import com.danghung.elearning.viewmodel.WritingVM.Companion.GET_QUESTIONS_WRITING
import com.danghung.elearning.viewmodel.WritingVM.Companion.REQUEST_WRITING_FEEDBACK
import com.danghung.elearning.viewmodel.WritingVM.Companion.SUBMIT_WRITING
import com.google.gson.Gson

class WritingFragment : BaseFragment<FragmentWritingBinding, WritingVM>(), SubmitSuccessDialog.OnResultCallBack {

    companion object {
        val TAG: String = WritingFragment::class.java.name
        private const val MIN_EVALUATABLE_WORDS = 20
    }

    private lateinit var startData: StartPracticeData
    private var currentQuestionId: Long = 0
    private var questions: List<QuestionRes> = emptyList()
    private var currentQuestionIndex = 0
    private val taskResults = mutableListOf<WritingTaskResultReq>()
    private var autoSubmissionInProgress = false
    private val gson by lazy { Gson() }

    override fun getClassVM(): Class<WritingVM> = WritingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWritingBinding =
        FragmentWritingBinding.inflate(inflater, container, false)

    override fun initViews() {
        startData = data as StartPracticeData

        callBack.showLoad()
        //callBack.showLoading()
        viewModel.getQuestions(startData.examPartRes?.id ?: 0)

        startCountdown()
        binding.btnSubmit.setOnClickListener(this)
        setupWordCounter()
        onBack()
    }
    private fun startCountdown() {
        viewModel.startCountdown(duration)
        viewModel.formattedTime.observe(viewLifecycleOwner) { time ->
            binding.tvTime.text = time
            if (time == "00:00") onTimeUp()
        }
    }

    private val duration: Int
        get() = startData.examPartRes?.duration ?: 0

    private fun getEssay(): String =
        binding.edtWriting.text?.toString()?.trim().orEmpty()

    private fun getWordCount(text: String): Int =
        if (text.isBlank()) 0 else text.split("\\s+".toRegex()).size

    private fun setupWordCounter() {
        binding.edtWriting.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val words = getWordCount(s?.toString().orEmpty())
                binding.tvToTalWords.text = "$words words"
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun clickView(v: View) {
        if (v.id == binding.btnSubmit.id) handleSubmit()
    }

    //Submit essay
    @SuppressLint("SetTextI18n")
    private fun handleSubmit() {
        if (viewModel.isTimeUp()) {
            notify("Time is up!")
            return
        }

        submitEssay(isAuto = false)
    }

    private fun onTimeUp() {
        if (viewModel.isAutoSubmitted()) return
        viewModel.markAutoSubmitted()
        autoSubmissionInProgress = true

        if (getEssay().isBlank()) {
            taskResults += emptyTaskResult(currentQuestionId)
            submitWritingResults()
        } else {
            submitEssay(isAuto = true)
        }
    }

    private fun submitEssay(isAuto: Boolean) {
        val essay = getEssay()
        val wordCount = getWordCount(essay)

        if (!isAuto && essay.isBlank()) {
            notify("Please write your essay!")
            return
        }

        if (!isAuto && wordCount < MIN_EVALUATABLE_WORDS) {
            notify(getString(R.string.txt_writing_too_short, MIN_EVALUATABLE_WORDS))
            return
        }

        disableSubmitButton()
        callBack.showLoading()

        if (isAuto) notify("Time is up! Auto submitting...")

        viewModel.evaluateWriting(
            essay = essay,
            questionId = currentQuestionId,
            wordCount = wordCount,
            duration = duration
        )
    }
    private fun onBack() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val dialog = ExitDialog(requireContext())
                    dialog.setCallBack(object : ExitDialog.OnExitCallBack {
                        override fun onExit() {
                            viewModel.stopCountdown()
                            callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)

                            // Hoặc nếu muốn pop fragment thông thường:
                            // parentFragmentManager.popBackStack()
                        }
                    })

                    dialog.show()
                }
            })
    }


    // API
    @Suppress("UNCHECKED_CAST")
    override fun apiSuccess(key: String, data: Any?) {
        when (key) {

            GET_QUESTIONS_WRITING -> {
                callBack.hideLoad()
                questions = (data as List<QuestionRes>).sortedWith(
                    compareBy<QuestionRes> { questionOrder(it.section) }.thenBy { it.id }
                )
                if (questions.isEmpty()) {
                    notify("No Writing tasks available")
                    disableSubmitButton()
                    return
                }
                currentQuestionIndex = 0
                showCurrentQuestion()
            }

            EVALUATE_WRITING -> {
                val result = data as IELTSWritingRes
                handleEvaluationResult(result)
            }

            SUBMIT_WRITING -> {
                callBack.hideLoading()
                if (startData.userExamRes != null) {
                    showFullTestProgressDialog()
                } else {
                    val result = data as WritingResultRes
                    showWritingScore(result.overallBand)
                }
            }

            REQUEST_WRITING_FEEDBACK -> {
                callBack.hideLoading()
                callBack.showFragment(
                    WritingHistoryFragment.TAG,
                    startData.userExamPartId,
                    false
                )
            }
        }
    }

    override fun handleApiError(key: String, code: Int, error: ApiErrorRes?) {
        callBack.hideLoading()
        enableSubmitButton()
        if (key == EVALUATE_WRITING && (code == 400 || code == 422)) return

        val dialog = ErrorDialog(requireContext())
        dialog.setCallBack(this)
        dialog.show()
    }

    override fun onExit() {
        requireActivity().finish()
    }

    override fun onRetry() {
        callBack.showLoading()
        viewModel.retryLastApi()
    }

    override fun onResult() {
        if (startData.userExamRes != null) {
            callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
        } else {
            callBack.showFragment(WritingHistoryFragment.TAG, startData.userExamPartId, false)
        }
    }

    private fun handleEvaluationResult(result: IELTSWritingRes) {
        val essay = getEssay()
        val criteria = result.criteriaScores

        val taskResult = WritingTaskResultReq(
            questionId = currentQuestionId,
            essayText = essay,
            wordCount = getWordCount(essay),
            duration = duration,
            overallBand = result.overallBand,
            taskAchievement = criteria?.taskAchievement,
            coherenceCohesion = criteria?.coherenceCohesion,
            lexicalResource = criteria?.lexicalResource,
            grammaticalRange = criteria?.grammaticalRange,
            detailedFeedback = gson.toJson(result.detailedFeedback),
            examinerFeedback = result.examinerFeedback,
            confidence = result.confidence,
            strengths = result.strengths,
            areasForImprovement = result.areasForImprovement,
            evidence = result.evidence,
            review = result.review,
            retrievalDocumentIds = result.retrievalDocumentIds,
            retrievalIndexVersion = result.retrievalIndexVersion
        )

        taskResults.removeAll { it.questionId == currentQuestionId }
        taskResults += taskResult

        val hasNextTask = currentQuestionIndex < questions.lastIndex
        if (hasNextTask && !autoSubmissionInProgress) {
            currentQuestionIndex++
            callBack.hideLoading()
            showCurrentQuestion()
            notify("Task ${currentQuestionIndex} saved. Continue with Task ${currentQuestionIndex + 1}.")
            return
        }

        submitWritingResults()
    }

    private fun submitWritingResults() {
        viewModel.stopCountdown()
        if (autoSubmissionInProgress) binding.tvTime.text = "00:00"

        val request = SubmitWritingExamReq(
            userExamPartId = startData.userExamPartId,
            taskResults = taskResults.toList()
        )
        viewModel.submitWriting(request)
    }

    @SuppressLint("SetTextI18n")
    private fun showCurrentQuestion() {
        val question = questions[currentQuestionIndex]
        currentQuestionId = question.id
        val taskNumber = currentQuestionIndex + 1
        val minimumWords = if (question.section.equals("task2", ignoreCase = true)) 250 else 150

        binding.tvWritingTestTitle.text = "Writing Task $taskNumber of ${questions.size}"
        binding.tvWritingQuestion.text = Html.fromHtml(
            question.content ?: "No question available",
            Html.FROM_HTML_MODE_COMPACT
        )
        binding.edtWriting.text?.clear()
        binding.edtWriting.hint = "Write your answer here (minimum $minimumWords words)..."
        binding.tvSubmitLabel.text = if (currentQuestionIndex < questions.lastIndex) {
            "SUBMIT TASK $taskNumber & CONTINUE"
        } else {
            "SUBMIT WRITING TEST"
        }
        enableSubmitButton()
    }

    private fun questionOrder(section: String?): Int = when (section?.lowercase()) {
        "task1" -> 1
        "task2" -> 2
        else -> 3
    }

    private fun emptyTaskResult(questionId: Long) = WritingTaskResultReq(
        questionId = questionId,
        essayText = "",
        wordCount = 0,
        duration = duration,
        overallBand = 0.0,
        taskAchievement = 0.0,
        coherenceCohesion = 0.0,
        lexicalResource = 0.0,
        grammaticalRange = 0.0,
        detailedFeedback = "Time expired before this task was completed.",
        examinerFeedback = "No answer submitted."
    )

    private fun disableSubmitButton() {
        binding.btnSubmit.apply {
            isEnabled = false
            isClickable = false
            backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.color_splash_des)
        }
    }

    private fun enableSubmitButton() {
        binding.btnSubmit.apply {
            isEnabled = true
            isClickable = true
            backgroundTintList = null
        }
    }

    private fun showFullTestProgressDialog() {
        val nextData = FullTestFlow.getNextStartData(startData)
        if (nextData == null) {
            notify("Full test completed!")
            val dialog = SubmitSuccessDialog(requireContext())
            dialog.setCallBack(this)
            dialog.show()
            return
        }

        val dialog = FullTestNextDialog(
            requireContext(),
            title = "Continue full test?",
            nextLabel = "NEXT",
            exitLabel = "EXIT"
        )
        dialog.setCallBack(object : FullTestNextDialog.OnFullTestNextCallBack {
            override fun onNext() {
                val nextTag = FullTestFlow.getFragmentTag(nextData.examPartRes?.skillType)
                if (nextTag == null) {
                    notify("Unsupported skill type")
                    callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
                    return
                }
                callBack.showFragment(nextTag, nextData, false)
            }

            override fun onExit() {
                callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
            }
        })
        dialog.show()
    }

    private fun showWritingScore(score: Double?) {
        ScoreResultDialog(
            context = requireContext(),
            skillName = "Writing",
            score = score,
            onRequestFeedback = {
                callBack.showLoading()
                viewModel.requestWritingFeedback(startData.userExamPartId)
            },
            onDone = {
                callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
            }
        ).show()
    }

    override fun onDestroy() {
        viewModel.stopCountdown()
        super.onDestroy()
    }
}
