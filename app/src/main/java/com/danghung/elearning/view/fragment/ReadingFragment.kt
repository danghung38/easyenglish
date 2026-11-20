package com.danghung.elearning.view.fragment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.danghung.elearning.R
import com.danghung.elearning.api.req.AnswerRLPartReq
import com.danghung.elearning.api.res.QuestionRes
import com.danghung.elearning.api.res.SubmitRLPartRes
import com.danghung.elearning.databinding.FragmentReadingBinding
import com.danghung.elearning.model.StartPracticeData
import com.danghung.elearning.view.adapter.ReadingAdapter
import com.danghung.elearning.view.dialog.ExitDialog
import com.danghung.elearning.view.dialog.FullTestNextDialog
import com.danghung.elearning.view.dialog.ScoreResultDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_EXAMS
import com.danghung.elearning.viewmodel.ReadingVM
import com.danghung.elearning.viewmodel.ReadingVM.Companion.GET_QUESTIONS_READING
import com.danghung.elearning.viewmodel.ReadingVM.Companion.SUBMIT_READING
import java.util.Locale


class ReadingFragment : BaseFragment<FragmentReadingBinding, ReadingVM>(),
    ReadingAdapter.OnAnswerSelectedListener {
    companion object {
        val TAG: String = ReadingFragment::class.java.name
    }

    private lateinit var startData: StartPracticeData
    private lateinit var readingAdapter: ReadingAdapter

    override fun getClassVM(): Class<ReadingVM> = ReadingVM::class.java

    override fun initViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentReadingBinding = FragmentReadingBinding.inflate(inflater, container, false)

    override fun initViews() {
        startData = data as StartPracticeData

        readingAdapter = ReadingAdapter(emptyList(), this)
        binding.rvReading.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = readingAdapter
        }

        viewModel.getQuestions(startData.examPartRes?.id ?: 0)
        callBack.showLoad()
        //callBack.showLoading()


        viewModel.formattedTime.observe(viewLifecycleOwner) { time ->
            binding.tvTime.text = time
            if (time == "00:00") {
                onTimeUp()
            }
        }
        binding.btnSubmit.setOnClickListener(this)
        onBack()
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

    private fun onTimeUp() {
        if (viewModel.isAutoSubmitted()) return
        disableSubmitButton()
        viewModel.markAutoSubmitted()
        notify("Time is up! Auto submitting...")
        viewModel.submit(startData.userExamPartId)
    }


    @SuppressLint("SetTextI18n")
    @Suppress("UNCHECKED_CAST")
    override fun handleApiSuccess(key: String, data: Any?) {
        when (key) {
            GET_QUESTIONS_READING -> {
                val questions = data as List<QuestionRes>
                readingAdapter.submitList(questions)
                binding.tvProcess.text = "0/${readingAdapter.answerableCount}"
                viewModel.startCountdown(startData.examPartRes?.duration ?: 15)
            }

            SUBMIT_READING -> {
                val result = data as SubmitRLPartRes
                if (startData.userExamRes != null) {
                    showFullTestProgressDialog(result.score)
                } else {
                    showScoreResult(result.score)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onAnswerSelected(answer: AnswerRLPartReq) {
        viewModel.addOrUpdateAnswer(answer)

        val answered = viewModel.getAnsweredCount()
        val total = readingAdapter.answerableCount
        binding.tvProcess.text = "$answered/$total"
    }

    override fun clickView(v: View) {
        when (v.id) {
            binding.btnSubmit.id -> handleSubmit()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSubmit() {
        if (viewModel.isTimeUp()) {
            notify("Time is up!")
            return
        }
        val error = viewModel.validateBeforeSubmit()
        if (error != null) {
            notify(error)
            return
        }
        viewModel.stopCountdown()
        binding.tvTime.text = "00:00"
        disableSubmitButton()
        callBack.showLoad()
        //callBack.showLoading()
        viewModel.submit(startData.userExamPartId)
    }

    private fun disableSubmitButton() {
        binding.btnSubmit.apply {
            isEnabled = false
            isClickable = false
            backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.color_splash_des)
        }
    }

    private fun showFullTestProgressDialog(score: Double?) {
        val nextData = FullTestFlow.getNextStartData(startData)
        if (nextData == null) {
            showScoreResult(score)
            return
        }

        val dialog = FullTestNextDialog(
            requireContext(),
            title = "Reading band: ${formatBand(score)}\nContinue full test?",
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

    private fun showScoreResult(score: Double?) {
        ScoreResultDialog(requireContext(), "Reading", score) {
            callBack.showFragment(MenuFragment.TAG, TYPE_EXAMS, false)
        }.show()
    }

    private fun formatBand(score: Double?): String =
        score?.let { String.format(Locale.US, "%.1f", it) } ?: "--"

    override fun onDestroy() {
        viewModel.stopCountdown()
        super.onDestroy()
    }

}
