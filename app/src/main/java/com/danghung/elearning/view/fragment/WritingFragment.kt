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
import com.danghung.elearning.databinding.FragmentWritingBinding
import com.danghung.elearning.model.StartPracticeData
import com.danghung.elearning.view.dialog.ErrorDialog
import com.danghung.elearning.view.dialog.SubmitSuccessDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.WritingVM
import com.danghung.elearning.viewmodel.WritingVM.Companion.EVALUATE_WRITING
import com.danghung.elearning.viewmodel.WritingVM.Companion.GET_QUESTIONS_WRITING
import com.danghung.elearning.viewmodel.WritingVM.Companion.SUBMIT_WRITING
import com.google.gson.Gson

class WritingFragment : BaseFragment<FragmentWritingBinding, WritingVM>(), SubmitSuccessDialog.OnResultCallBack {

    companion object {
        val TAG: String = WritingFragment::class.java.name
    }

    private lateinit var startData: StartPracticeData
    private var currentQuestionId: Long = 0
    private val gson by lazy { Gson() }

    override fun getClassVM(): Class<WritingVM> = WritingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWritingBinding =
        FragmentWritingBinding.inflate(inflater, container, false)

    override fun initViews() {
        startData = data as StartPracticeData

        callBack.showLoading()
        viewModel.getQuestions(startData.examPartRes?.id ?: 0)

        startCountdown()
        binding.btnSubmit.setOnClickListener(this)
        setupWordCounter()
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

        viewModel.stopCountdown()
        binding.tvTime.text = "00:00"
        submitEssay(isAuto = false)
    }

    private fun onTimeUp() {
        if (viewModel.isAutoSubmitted()) return
        viewModel.markAutoSubmitted()
        submitEssay(isAuto = true)
    }

    private fun submitEssay(isAuto: Boolean) {
        val essay = getEssay()

        if (!isAuto && essay.isBlank()) {
            notify("Please write your essay!")
            return
        }

        disableSubmitButton()
        callBack.showLoading()

        if (isAuto) notify("Time is up! Auto submitting...")

        viewModel.evaluateWriting(
            essay = essay,
            questionId = currentQuestionId,
            wordCount = getWordCount(essay),
            duration = duration
        )
    }

    // API
    @Suppress("UNCHECKED_CAST")
    override fun apiSuccess(key: String, data: Any?) {
        when (key) {

            GET_QUESTIONS_WRITING -> {
                callBack.hideLoading()
                val questions = data as List<QuestionRes>
                val firstQuestion = questions.firstOrNull()
                currentQuestionId = firstQuestion?.id ?: 0

                binding.tvWritingQuestion.text = Html.fromHtml(
                    firstQuestion?.content ?: "No question available",
                    Html.FROM_HTML_MODE_COMPACT
                )
            }

            EVALUATE_WRITING -> {
                val result = data as IELTSWritingRes
                handleEvaluationResult(result)
            }

            SUBMIT_WRITING -> {
                callBack.hideLoading()
                notify("Submit writing part successfully!")
                var dialog = SubmitSuccessDialog(requireContext())
                dialog.setCallBack(this)
                dialog.show()
            }
        }
    }

    override fun handleApiError(key: String, code: Int, error: ApiErrorRes?) {
        var dialog = ErrorDialog(requireContext())
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
        callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
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
            examinerFeedback = result.examinerFeedback
        )

        val request = SubmitWritingExamReq(
            userExamPartId = startData.userExamPartId,
            overallBand = result.overallBand ?: 0.0,
            taskResults = listOf(taskResult)
        )
        viewModel.submitWriting(request)
    }

    private fun disableSubmitButton() {
        binding.btnSubmit.apply {
            isEnabled = false
            isClickable = false
            backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.color_splash_des)
        }
    }

    override fun onDestroy() {
        viewModel.stopCountdown()
        super.onDestroy()
    }
}
