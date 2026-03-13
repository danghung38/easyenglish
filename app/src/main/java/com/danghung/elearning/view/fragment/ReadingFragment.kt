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
import com.danghung.elearning.view.dialog.SubmitSuccessDialog
import com.danghung.elearning.view.fragment.MenuFragment.Companion.TYPE_LEARN
import com.danghung.elearning.viewmodel.ReadingVM
import com.danghung.elearning.viewmodel.ReadingVM.Companion.GET_QUESTIONS_READING
import com.danghung.elearning.viewmodel.ReadingVM.Companion.SUBMIT_READING


class ReadingFragment : BaseFragment<FragmentReadingBinding, ReadingVM>(),
    ReadingAdapter.OnAnswerSelectedListener, SubmitSuccessDialog.OnResultCallBack {
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
        callBack.showLoading()


        viewModel.formattedTime.observe(viewLifecycleOwner) { time ->
            binding.tvTime.text = time
            if (time == "00:00") {
                onTimeUp()
            }
        }
        binding.btnSubmit.setOnClickListener(this)
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
                binding.tvProcess.text = "0/${questions.size}"
                viewModel.startCountdown(startData.examPartRes?.duration ?: 15)
            }

            SUBMIT_READING -> {
                data as SubmitRLPartRes
                notify("Submit reading part successfully!")
                var dialog = SubmitSuccessDialog(requireContext())
                dialog.setCallBack(this)
                dialog.show()
            }
        }
    }

    override fun onResult() {
        callBack.showFragment(MenuFragment.TAG, TYPE_LEARN, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onAnswerSelected(answer: AnswerRLPartReq) {
        viewModel.addOrUpdateAnswer(answer)

        val answered = viewModel.getAnsweredCount()
        val total = readingAdapter.itemCount
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
        callBack.showLoading()
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

    override fun onDestroy() {
        viewModel.stopCountdown()
        super.onDestroy()
    }

}